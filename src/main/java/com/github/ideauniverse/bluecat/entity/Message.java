package com.github.ideauniverse.bluecat.entity;

import lombok.Data;

@Data
public class Message{

    private String id;

    private Object content;

    private String senderId;

    private String receiverId;

    private Integer type;
}
