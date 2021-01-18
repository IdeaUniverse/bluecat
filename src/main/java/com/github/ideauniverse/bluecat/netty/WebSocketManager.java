package com.github.ideauniverse.bluecat.netty;

import com.alibaba.fastjson.JSON;
import com.github.ideauniverse.bluecat.entity.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 管理者
 */
@Slf4j
public class WebSocketManager {

    public static ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 给某个接收者发送消息
     * @param message
     */
    public static void sendMessage(Message<?> message) {
        Channel channel = channels.get(message.getReceiverId());
        if(channel == null ){
            return;
        }
        if(!channel.isActive()){
            channels.remove(message.getReceiverId(), channel);
            return;
        }
        String text = JSON.toJSONString(message);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(text);
        channel.writeAndFlush(textWebSocketFrame);
        log.info("消息发送成功, {}", text);
    }

    /**
     * 给所有人发送消息
     * @param message
     */
    public static void broadCast(Message<?> message) {
        String text = JSON.toJSONString(message);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(text);
        channels.forEach((key, channel) -> {
            if(!channel.isActive()){
                channels.remove(key, channel);
                return;
            }
            channel.writeAndFlush(textWebSocketFrame);
            log.info("消息广播成功, {}", text);
        });
    }
}
