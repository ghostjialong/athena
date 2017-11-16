package com.athena.server.netty.handler;

import com.athena.exchange.MessageDeliver;
import com.athena.protobuf.RequestEntity;
import com.athena.client.ClientIdentity;
import com.athena.protobuf.ResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */

@Component
public class MessageHandler extends ChannelInboundHandlerAdapter {

    Logger logger = Logger.getLogger(MessageHandler.class.toString());

    @Autowired
    public MessageDeliver messageDeliver;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        //MessageEntity.Message message = (MessageEntity.Message) msg;

        RequestEntity.Request request = (RequestEntity.Request) msg;

        switch (request.getRequestType()) {
            case SUBSCRIBE:
                // 订阅消息， 生成订阅者client, 将通信信道包含其中
                ClientIdentity clientIdentity = new ClientIdentity(request.getClientId(), ctx.channel());
                logger.info("subscribe request got, data: " + request.toString());
                messageDeliver.subscribe(request.getClientId(), clientIdentity);
                ByteBuf byteBuf = Unpooled.buffer(1024);
                ResponseEntity.Response response = ResponseEntity.Response.newBuilder()
                        .setCode("1000")
                        .setMessage("You are an excellent engineer, and your proposal is processed successfully!")
                        .build();
                byteBuf.writeBytes(response.toByteArray());
                ctx.channel().writeAndFlush(byteBuf);
                break;
            case PUB:
                // 客户端发布消息
                logger.info("client send to message");
                messageDeliver.pubMessage(request.getMessage());
                break;
            case ACK:
                logger.info("message got by server: " + request.toString());
            default:
                break;
        }

    }
}
