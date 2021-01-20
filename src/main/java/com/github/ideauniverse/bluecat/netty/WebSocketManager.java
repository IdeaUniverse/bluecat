package com.github.ideauniverse.bluecat.netty;

import com.alibaba.fastjson.JSON;
import com.github.ideauniverse.bluecat.entity.Message;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 管理者
 */
@Slf4j
public class WebSocketManager {

    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 发送消息
     * @param message
     */
    public static void broadCast(Message<?> message) {
        String text = JSON.toJSONString(message);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(text);
        channelGroup.writeAndFlush(textWebSocketFrame);
        log.info("消息广播成功, {}", text);
    }
}
