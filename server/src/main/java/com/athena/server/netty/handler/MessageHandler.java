package com.athena.server.netty.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.athena.idalloc.api.IdRequest;
import com.athena.idalloc.client.IdallocClient;
import com.athena.exchange.MessageDeliver;
import com.athena.protobuf.MessageEntity;
import com.athena.protobuf.RequestEntity;
import com.athena.client.ClientIdentity;
import com.athena.protobuf.ResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */

@Component("messageHandler")
@ChannelHandler.Sharable
public class MessageHandler extends ChannelInboundHandlerAdapter {

    Logger logger = Logger.getLogger(MessageHandler.class.toString());

    @Autowired
    public MessageDeliver messageDeliver;

    @Autowired
    public IdallocClient idallocClient;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        //MessageEntity.Message message = (MessageEntity.Message) msg;
        RequestEntity.Request request = (RequestEntity.Request) msg;

        switch (request.getRequestType()) {
            case SUBSCRIBE:
                // 订阅消息， 生成订阅者client, 将通信信道包含其中
                ClientIdentity clientIdentity = new ClientIdentity(request.getClientId(), ctx.channel());
                logger.info("subscribe request got, data: " + request.toString());
                // 消息类型分为订阅私人消息和群组消息
                int groupId = request.getGroupId();
                if (request.hasGroupId() && groupId != 0) {
                    // 订阅的是群消息
                    messageDeliver.subscribe(clientIdentity, groupId);
                } else {
                    // 订阅私有消息
                    messageDeliver.subscribe(clientIdentity);
                }

                ByteBuf byteBuf = Unpooled.buffer(1024);
                ResponseEntity.Response response = ResponseEntity.Response.newBuilder()
                        .setCode("1000")
                        .setRequestId(request.getRequestId())
                        .setMessage("You are an excellent engineer, and your proposal is processed successfully!")
                        .build();
                byteBuf.writeBytes(response.toByteArray());
                ctx.channel().writeAndFlush(byteBuf);
                break;
            case PUB:
                // 客户端发布消息
                logger.info("client send to message");
                // 生成messageId
                long messageId= generateMessageId();
                MessageEntity.Message userMessage = addMessageId(request.getMessage(), messageId);
                // 构造消息message 实体
                //request.
                // 广播消息至其他az组
                logger.info("msg entity: " + request.getMessage().getMessageId());
                if (request.getMessage().getType().equals(MessageEntity.messageType.TO_GROUP)) {
                    messageDeliver.pubMessage(userMessage, userMessage.getGroupId());
                } else {
                    messageDeliver.pubMessage(userMessage);
                }
                break;
            case ACK:
                logger.info("Ack message got by server: " + request.toString());
                Long messageId2 = request.getMessageId();
                messageDeliver.ackMessage(messageId2);
                break;
            default:
                break;
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.info("fuck......");
        //cause.printStackTrace();
    }

    private MessageEntity.Message addMessageId(MessageEntity.Message message, long messageId) {
        MessageEntity.Message messageWithId = MessageEntity.Message.newBuilder()
                .setMessageId(messageId)
                .setRecipientId(message.getRecipientId())
                .setGroupId(message.getGroupId())
                .setSenderId(message.getSenderId())
                .setBody(message.getBody())
                .setHeader(message.getHeader())
                .setType(message.getType())
                .build();
        return messageWithId;
    }

    public Long generateMessageId()  {
        Long result;
        try {
            //result = idallocDubbo.idAllocForRequest
            //result = IdallocClient.requestIdAlloc();
            result = idallocClient.requestIdAllocDubbo();
        } catch (Exception e) {
            System.out.println(e);
            result = Long.valueOf(-1);
        }
        return result;
    }
}
