package com.github.ideauniverse.bluecat.netty;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.ideauniverse.bluecat.common.BlueCatCollection;
import com.github.ideauniverse.bluecat.common.Constants;
import com.github.ideauniverse.bluecat.dao.NewsDao;
import com.github.ideauniverse.bluecat.entity.Message;
import com.github.ideauniverse.bluecat.entity.News;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
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
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    @Autowired
    private NewsDao newsDao;

    @Autowired
    private BlueCatCollection<News> collection;

    private WebSocketServerHandshaker handShaker;

    /**
     * 处理客户端与服务端之前的websocket业务
     * @param ctx
     * @param frame
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
        //判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //判断是否是二进制消息，如果是二进制消息暂不处理
        if(!(frame instanceof TextWebSocketFrame) ){
            return;
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        Message<?> message = JSON.parseObject(((TextWebSocketFrame) frame).text(), new TypeReference<>(){});
        handleMessage(ctx, message);
    }

    /**
     * WebSocket 消息处理
     * @param ctx
     * @param message
     */
    private void handleMessage(ChannelHandlerContext ctx, Message<?> message){
        if(message.getType() == Constants.MESSAGE_TYPE_HANDSHAKE){
            String userId = UUID.randomUUID().toString();
            WebSocketManager.channels.put(userId, ctx.channel());
            Message<String> newsMessage = new Message<>();
            newsMessage.setId(UUID.randomUUID().toString());
            newsMessage.setContent(userId);
            newsMessage.setReceiverId(userId);
            newsMessage.setType(Constants.MESSAGE_TYPE_HANDSHAKE);
            WebSocketManager.sendMessage(newsMessage);
        }else if(message.getType() == Constants.MESSAGE_TYPE_LIST){    // 前端查询事件
            List<News> newsList = newsDao.queryAll();   // 查询数据
            collection.setCollection(newsList);     // 把数据添加进集合
            Message<List<News>> newsMessage = new Message<>();
            newsMessage.setId(UUID.randomUUID().toString());
            newsMessage.setContent(collection.getCollection());
            newsMessage.setType(Constants.MESSAGE_TYPE_LIST);
            newsMessage.setReceiverId(message.getSenderId());
            WebSocketManager.sendMessage(newsMessage);
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

    /**
     * 处理客户端向服务端发起http握手请求的业务
     * @param ctx
     * @param req
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
        if (!req.decoderResult().isSuccess()
                || !("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                req.uri(), null, false);
        handShaker = wsFactory.newHandshaker(req);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            handShaker.handshake(ctx.channel(), req);
        }
    }

    /**
     * 服务端向客户端响应消息
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                  DefaultFullHttpResponse res){
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object msg) {
        //处理客户端向服务端发起http握手请求的业务
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(context,  (FullHttpRequest)msg);
        }else if (msg instanceof WebSocketFrame) { //处理websocket连接业务
            handWebsocketFrame(context, (WebSocketFrame)msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("client is active !");
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