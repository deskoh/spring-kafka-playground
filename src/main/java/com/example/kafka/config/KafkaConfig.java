package com.example.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

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
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
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
}