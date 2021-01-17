package com.github.ideauniverse.bluecat.netty;

import com.alibaba.fastjson.JSON;
import com.github.ideauniverse.bluecat.entity.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketManager {

    public static ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    public static void sendMessage(Message message) {
        Channel channel = channels.get(message.getReceiverId());
        if(channel != null && channel.isActive()){
            String text = JSON.toJSONString(message);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(text);
            channel.writeAndFlush(textWebSocketFrame);
            log.info("消息发送成功, {}", text);
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
