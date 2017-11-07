package com.athena.server.netty.handler;

import com.athena.protobuf.MessageEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */
public class MessageHandler extends ChannelInboundHandlerAdapter {

    Logger logger = Logger.getLogger(MessageHandler.class.toString());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        MessageEntity.Message message = (MessageEntity.Message) msg;

        logger.info("test message: " + message.toString());
    }
}
