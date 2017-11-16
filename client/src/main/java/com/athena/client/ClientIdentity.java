package com.athena.client;


import com.athena.protobuf.MessageEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Created by wangjialong on 11/13/17.
 */
public class ClientIdentity {

    private long clientId;

    private Channel channel;

    public ClientIdentity(long clientId, Channel channel) {
        this.clientId = clientId;
        this.channel  = channel;
    }

    public long getClientId() {
        return clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void consumeMessage(MessageEntity.Message message) {
        ByteBuf byteBuf = Unpooled.buffer(1024);

        byteBuf.writeBytes(message.toByteArray());
        channel.writeAndFlush(byteBuf);
    }
}
