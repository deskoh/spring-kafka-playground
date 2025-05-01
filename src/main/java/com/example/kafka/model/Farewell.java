package com.example.kafka.model;

import lombok.Data;

@Data
public class Farewell {
    private String message;
    private int remainingMinutes;
}