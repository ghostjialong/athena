package com.athena.server.netty;

import com.athena.protobuf.MessageEntity;
import com.athena.server.netty.handler.MessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 * Created by wangjialong on 11/7/17.
 */
public class NettyServer {

    private static int port;

    static {
        port = 8069;
    }

    public void start() {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(workGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        public void initChannel(SocketChannel ch){
                            ch.pipeline().addLast(new ProtobufDecoder(MessageEntity.Message.getDefaultInstance()))
                                    .addLast(new MessageHandler());
                        }
                    }).childOption(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch(InterruptedException e) {

        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



}
