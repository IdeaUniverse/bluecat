package com.github.ideauniverse.bluecat.netty;


import com.alibaba.fastjson.JSON;
import com.github.ideauniverse.bluecat.common.BlueCatCollection;
import com.github.ideauniverse.bluecat.common.Constants;
import com.github.ideauniverse.bluecat.entity.BlueCatEntity;
import com.github.ideauniverse.bluecat.entity.Message;
import com.github.ideauniverse.bluecat.entity.News;
import com.github.ideauniverse.bluecat.service.NewsService;
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
    private NewsService newsService;

    @Autowired
    private BlueCatCollection collection;

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
        Message message = JSON.parseObject(((TextWebSocketFrame) frame).text(), Message.class);
        handleMessage(ctx, message);
    }

    private void handleMessage(ChannelHandlerContext ctx, Message message){
        // 连接请求
//        BlueCatCollection collection = new BlueCatCollection();
        if(message.getType() == Constants.MESSAGE_TYPE_CONNECT){
            try{
                WebSocketManager.channels.put(message.getSenderId(), ctx.channel());
                List<News> newsList = newsService.queryAll();   // 查询数据
                collection.setCollection(newsList);     // 把数据添加进集合
                Message newsMessage = new Message();
                newsMessage.setId(UUID.randomUUID().toString());
                newsMessage.setContent(collection.getCollection());
                newsMessage.setType(Constants.MESSAGE_TYPE_NEWS_LIST);
                newsMessage.setSenderId(message.getSenderId());
                WebSocketManager.sendMessage(newsMessage);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void sendMessage(Channel channel, Message message){
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(message));
        channel.writeAndFlush(textWebSocketFrame);
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
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("client is inactive !");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e){
        log.error("netty exception caught, {}", e.getMessage());
        ctx.close();
    }
}