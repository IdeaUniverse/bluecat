package com.github.ideauniverse.bluecat.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.ideauniverse.bluecat.common.BlueCatCollection;
import com.github.ideauniverse.bluecat.common.Constants;
import com.github.ideauniverse.bluecat.dao.NewsDao;
import com.github.ideauniverse.bluecat.entity.Message;
import com.github.ideauniverse.bluecat.entity.News;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

/**
 * netty服务端处理器
 **/
@Slf4j
@ChannelHandler.Sharable
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private BlueCatCollection<News> collection;

    /**
     * WebSocket 消息处理
     * @param ctx
     * @param message
     */
    private void handleMessage(ChannelHandlerContext ctx, Message<?> message){
        if(message.getType() == Constants.MESSAGE_TYPE_HANDSHAKE){
            String userId = UUID.randomUUID().toString();
            Message<String> newsMessage = new Message<>();
            newsMessage.setId(UUID.randomUUID().toString());
            newsMessage.setContent(userId);
            newsMessage.setReceiverId(userId);
            newsMessage.setType(Constants.MESSAGE_TYPE_HANDSHAKE);
            WebSocketManager.broadCast(newsMessage);
        }else if(message.getType() == Constants.MESSAGE_TYPE_LIST){    // 前端查询事件
            List<News> newsList = newsDao.queryAll();   // 查询数据
            collection.setCollection(newsList);     // 把数据添加进集合
            Message<List<News>> newsMessage = new Message<>();
            newsMessage.setId(UUID.randomUUID().toString());
            newsMessage.setContent(collection.getCollection());
            newsMessage.setType(Constants.MESSAGE_TYPE_LIST);
            newsMessage.setReceiverId(message.getSenderId());
            WebSocketManager.broadCast(newsMessage);
        }else if(message.getType() == Constants.MESSAGE_TYPE_CREATE){  // 前端创建事件
            News news = JSON.parseObject(message.getContent().toString(), News.class);
            collection.add(news);
        } else if(message.getType() == Constants.MESSAGE_TYPE_UPDATE){  // 前端修改事件
            News news = JSON.parseObject(message.getContent().toString(), News.class);
            collection.update(news);
        } else if(message.getType() == Constants.MESSAGE_TYPE_DELETE){  // 前端删除事件
            collection.deleteById(message.getContent().toString());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame webSocketFrame) {
        Message<?> message = JSON.parseObject(webSocketFrame.text(), new TypeReference<>(){});
        handleMessage(context, message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("client is active !");
        WebSocketManager.channelGroup.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("client is inactive !");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e){
        log.error("netty exception caught, {}", e.getMessage());
    }
}