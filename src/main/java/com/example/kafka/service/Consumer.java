package com.example.kafka.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Consumer {

    private static final String LISTENER_ID = "default";

    private static final String TOPIC = "demo-topic";

    private final KafkaListenerEndpointRegistry registry;

    public Consumer(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    // id: Spring Listener ID for starting / stopping listener manually, groupId
    // groupId: can be specified to override value in application.yaml, will follow id automatically if not set
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
                "Filter listener consumed event from topic [%s], partition [%d], offset [%d]: value = [%s]",
                topic, partition, offset, record.value()
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
}