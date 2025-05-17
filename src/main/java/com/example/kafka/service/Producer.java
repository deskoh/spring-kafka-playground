package com.example.kafka.service;

import com.example.kafka.model.Farewell;
import com.example.kafka.model.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Producer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Producer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // Optionally set default topic
        // this.kafkaTemplate.setDefaultTopic("...");
    }

    public void sendMessage(String topic, Object data) {
        /*
        Message<Object> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("__TypeId__", "greeting") // maps to __TypeId__
                .build();
        var future = this.kafkaTemplate.send(message);
        */

        var future = this.kafkaTemplate.send(topic, data);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message [{}] to topic [{}] with offset [{}]", data, topic, result.getRecordMetadata().offset());
            } else {
                log.error(ex.getMessage());
            }
        });
    }

    public void sendGreeting(String topic, Greeting greeting) {
        var future = this.kafkaTemplate.send(topic, greeting);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent greeting [{}] to topic [{}] with offset [{}]", greeting.getMsg(), topic, result.getRecordMetadata().offset());
            } else {
                log.error(ex.getMessage());
            }
        });
    }

    public void sendFarewell(String topic, Farewell farewell) {
        var future = this.kafkaTemplate.send(topic, farewell);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent farewell [{}] to topic [{}] with offset [{}]", farewell.getMessage(), topic, result.getRecordMetadata().offset());
            } else {
                log.error(ex.getMessage());
            }
        });
    }
}