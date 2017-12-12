package com.athena.server.netty.handler;

import com.athena.common.ResponseCode;
import com.athena.protobuf.RequestEntity;
import com.athena.protobuf.ResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Service;

/**
 * Created by wangjialong on 12/12/17.
 */
@Service
@ChannelHandler.Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RequestEntity.Request request = (RequestEntity.Request) msg;

        RequestEntity.PacketType requestType = request.getRequestType();

        if (requestType == RequestEntity.PacketType.PING) {
            // 构造心跳响应体
            ResponseEntity.Response response = ResponseEntity.Response.newBuilder()
                    .setCode(ResponseCode.PONG.toString())
                    .setMessage("PONG")
                    .setRequestId(request.getRequestId())
                    .build();
            ByteBuf byteBuf = Unpooled.buffer(1024);
            byteBuf.writeBytes(response.toByteArray());
            ctx.channel().writeAndFlush(byteBuf);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
