package com.example.kafka.controller;

import com.example.kafka.service.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/produce")
public class ProduceController {

    private final Producer producer;

    public ProduceController(Producer producer) {
        this.producer = producer;
    }

    // POST /api/produce?topic=<key>
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String produce(@RequestParam String topic, @RequestBody Map<String,Object> data) {
        this.producer.sendMessage(topic, data);
        return "OK";
    }

    // POST /api/produce?topic=<key>
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String producePlain(@RequestParam String topic, @RequestBody String data) {
        // You might want to wrap the string into a Map or process it directly
        this.producer.sendMessage(topic, data);
        return "OK";
    }

}
