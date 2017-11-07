package com.athena.client;

import com.athena.protobuf.MessageEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Created by wangjialong on 11/7/17.
 */
public class NettyClient {

    private int port;
    private String address;

    public NettyClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtobufEncoder());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(address, port);
            future.addListener((ChannelFuture future2)->{
                System.out.println("connect to server......");

                MessageEntity.Message message = MessageEntity.Message.newBuilder()
                        .setMessageId("105001676")
                        .setToUserId("105002955")
                        .setBody("Hello, I wanna chat with you.....").build();

                future2.channel().writeAndFlush(message);
                future2.channel().writeAndFlush(message);
            });
            future.channel().closeFuture().sync();
        } catch(Exception e) {
            System.out.println(e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
