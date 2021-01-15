package com.github.ideauniverse.bluecat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class News extends BlueCatEntity{

    private String title;

    private String content;

    private LocalDateTime createdAt;
}
