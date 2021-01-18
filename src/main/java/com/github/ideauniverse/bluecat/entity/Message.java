package com.github.ideauniverse.bluecat.entity;

import lombok.Data;

/**
 * 与前端通信的消息类
 */
@Data
public class Message<T>{

    private String id;  // 消息 id

    private T content; // 消息内容

    private String senderId;    // 消息发送者id

    private String receiverId;  // 消息接收者id

    private Integer type;       // 消息类型，对应Constants.MESSAGE_TYPE_..的值
}
