package com.example.kafka.config;

import com.example.kafka.model.Farewell;
import com.example.kafka.model.Greeting;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {


    /*
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }
    */

    @Bean
    public NewTopic topic1() {
        return new NewTopic("demo-topic", 2, (short) 1);
    }

    // Configure retries and send messages that cannot be processed to corresponding topics with `-dlt` suffix.
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<?,?> template) {
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new FixedBackOff(1000L, 3) // 3 retries with 1s delay
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> filterKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);

        // return true for message to be filtered out
        factory.setRecordFilterStrategy(record -> !record.value().toString().contains("important"));
        // Acknowledge discarded message
        factory.setAckDiscarded(true);

        // Common Error Handler and configurations for listener need to be set manually.
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Farewell> farwellKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            DefaultErrorHandler errorHandler
    ) {
        var consumerConfig = kafkaProperties.getConsumer();
        var props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, consumerConfig.getGroupId(),
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerConfig.getEnableAutoCommit()
        );

        var factory = new ConcurrentKafkaListenerContainerFactory<String, Farewell>();
        var consumerFactory = new DefaultKafkaConsumerFactory(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(Farewell.class, false)
        );
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
        return factory;
    }
}