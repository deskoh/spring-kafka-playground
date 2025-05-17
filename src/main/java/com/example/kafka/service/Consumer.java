package com.example.kafka.service;

import com.example.kafka.model.Farewell;
import com.example.kafka.model.Greeting;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Consumer {

    private static final String LISTENER_ID = "default";

    private static final String FAREWELL_LISTENER_ID = "farewell-group";

    private static final String TOPIC = "demo-topic";

    private final KafkaListenerEndpointRegistry registry;

    public Consumer(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Deserialization to specific target type without header information.
     * Does not use autoconfigured consumerFactory.
     */
    @KafkaListener(id = FAREWELL_LISTENER_ID, topics = TOPIC, containerFactory = "farwellKafkaListenerContainerFactory")
    public void farewellListener(Farewell data, Acknowledgment ack) {
        log.info(String.format(
                "Consumed farewell from topic [%s]: Message = [%s], RemainingMinutes = [%s]", TOPIC,
                data.getMessage(), data.getRemainingMinutes()
        ));
        ack.acknowledge();
    }

    /**
     * Listener that can be started or stopped manually using Listener ID.
     * idIsGroup set to false in order for Consumer Group ID to use value from application.yaml.
     */
    @KafkaListener(id = LISTENER_ID, idIsGroup = false, topics = TOPIC, autoStartup = "false")
    public void listen(
            ConsumerRecord<String, Object> record,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        log.info(String.format(
                "Consumed event from topic [%s], partition [%d], offset [%d]: value = [%s]",
                topic, partition, offset, record.value()
        ));
        ack.acknowledge();
    }

    /**
     * Listener that does message filtering.
     * Uses autoconfigured consumerFactory.
     */
    @KafkaListener(
            id = "demo-service-filter", // Group ID will follow id as idIsGroup defaults to true
            topics = TOPIC,
            containerFactory = "filterKafkaListenerContainerFactory"
    )
    public void listenWithFilter(
            ConsumerRecord<String, Object> record,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        log.info(String.format(
                "Filter listener consumed event from topic [%s], partition [%d], offset [%d]: value = [%s] (%s)",
                topic, partition, offset, record.value().toString(), record.value().getClass()
        ));
        ack.acknowledge();
    }

    public void startListener() {
        registry.getListenerContainer(LISTENER_ID).start();
    }

    public void stopListener() {
        registry.getListenerContainer(LISTENER_ID).stop();
    }

    @PreDestroy
    public void shutdownKafkaListeners() {
        log.info("Shutting down Kafka listener containers...");
        registry.getListenerContainers().forEach(container -> {
            if (container.isRunning()) {
                log.info("Stopping listener container {}", container.getListenerId());
                container.stop();
            }
        });
    }

    private static JavaType greetingType = TypeFactory.defaultInstance().constructType(Greeting.class);

    private static JavaType farewellType = TypeFactory.defaultInstance().constructType(Farewell.class);

    public static JavaType determineType(String topic, byte[] data, Headers headers) {
        log.info("Determining type...");
        if ("demo-topic-farewell".compareTo(topic) == 0) {
            return farewellType;
        }
        else if ("demo-topic-greeting".compareTo(topic) == 0) {
            return greetingType;
        }

        var payload = new String(data);
        if (payload.contains("remainingMinutes")) {
            return farewellType;
        }
        else if (payload.contains("name")) {
            return greetingType;
        }

        // Return null to use spring.json.value.default.type for fallback
        return null;
    }
}