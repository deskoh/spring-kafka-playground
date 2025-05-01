package com.example.kafka.service;

import com.example.kafka.model.Farewell;
import com.example.kafka.model.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(groupId = "multi", idIsGroup = false, topics = "demo-topic")
public class MultiConsumer {

    public static Object parser(String rawString, Headers headers) {
        // byte[] header = headers.lastHeader(ToStringSerializer.VALUE_TYPE).value();
        for (var header: headers) {
            log.debug("Header [{}={}]", new String(header.key()), new String(header.value()));
        }
        return rawString;
    }

    @KafkaHandler
    public void handleGreeting(Greeting greeting, Acknowledgment ack) {
        System.out.println("Greeting received: " + greeting);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleFarewell(Farewell farewell, Acknowledgment ack) {
        System.out.println("Farewell received: " + farewell);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleString(String string, Acknowledgment ack) {
        System.out.println("String received: " + string);
        ack.acknowledge();
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object, Acknowledgment ack) {
        System.out.println("Unkown type received: " + object);
        ack.acknowledge();
    }
}