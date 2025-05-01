package com.example.kafka.controller;

import com.example.kafka.service.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/consume")
public class ConsumeController {

    private final Consumer consumer;

    public ConsumeController(Consumer consumer) {
        this.consumer = consumer;
    }

    // PUT /api/consume/start
    @PutMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public String start() {
        this.consumer.startListener();
        return "Listener started";
    }

    // PUT /api/consume/stop
    @PutMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    public String stop() {
        this.consumer.stopListener();
        return "Listener stopped";
    }

}
