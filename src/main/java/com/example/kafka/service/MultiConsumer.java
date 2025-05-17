package com.example.kafka.service;

import com.example.kafka.model.Farewell;
import com.example.kafka.model.Greeting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(groupId = "multi", idIsGroup = false, topics = "demo-topic")
public class MultiConsumer {
    // Need to set spring.devtools.restart.enabled to false for KafkaHandler to resolve correctly as
    // different ClassLoader will cause reflection not to work correctly.
    public static Object parser(String rawString, Headers headers) throws JsonProcessingException {
        var objectMapper = JacksonUtils.enhancedObjectMapper();
        // byte[] header = headers.lastHeader(ToStringSerializer.VALUE_TYPE).value();
        for (var header: headers) {
            log.debug("Header [{}={}]", new String(header.key()), new String(header.value()));
        }
        if (rawString.contains("remainingMinutes")) {
            return objectMapper.readValue(rawString, Farewell.class);
        }
        else if (rawString.contains("name")) {
            var result = objectMapper.readValue(rawString, Greeting.class);
            return result;
        }
        return rawString;
    }


    @KafkaHandler
    public void handleGreeting(@Payload Greeting greeting, Acknowledgment ack) {
        log.info("Greeting received: " + greeting);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleFarewell(@Payload Farewell farewell, Acknowledgment ack) {
        log.info("Farewell received: " + farewell);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleString(@Payload String string, Acknowledgment ack) {
        log.info("String received: " + string);
        ack.acknowledge();
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object, Acknowledgment ack) {
        log.info("Unkown type received: " + object);
        ack.acknowledge();
    }
}