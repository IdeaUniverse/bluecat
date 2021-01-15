package com.github.ideauniverse.bluecat.netty;

import com.alibaba.fastjson.JSON;
import com.github.ideauniverse.bluecat.entity.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {

    public static ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    public static void sendMessage(Message message) {
        Channel channel = channels.get(message.getSenderId());
        if(channel != null){
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(message));
            channel.writeAndFlush(textWebSocketFrame);
        }
    }

    public static void broadCast(Message message) {
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(message));
        channels.forEach((key, channel) -> {
            if(channel.isActive()){
                channel.writeAndFlush(textWebSocketFrame);
            }
        });
    }
}
