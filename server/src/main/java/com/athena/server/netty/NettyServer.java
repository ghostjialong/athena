package com.athena.server.netty;

import com.athena.exchange.MessageDeliver;
import com.athena.protobuf.MessageEntity;
import com.athena.protobuf.RequestEntity;
import com.athena.server.netty.handler.MessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */
@Component("nettyServer")
public class NettyServer {

    private static int port;

    private Logger logger = Logger.getLogger("NettyServer.class");

    static {
        port = 8069;
    }

    public static Map<Channel, Long>  channelClientMap = new HashMap<>();

    @Autowired
    public MessageHandler messageHandler;

    public void start() {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(workGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        public void initChannel(SocketChannel ch){
                            ch.pipeline().addLast(new ProtobufDecoder(RequestEntity.Request.getDefaultInstance()))
                                    .addLast(new IdleStateHandler(5, 5,
                                            5,  TimeUnit.SECONDS) {
                                        @Override
                                        protected  void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
                                            Channel ch = ctx.channel();
                                            Long clientId = channelClientMap.get(ch);
                                            if (clientId != null ) {
                                                MessageDeliver.unSubscribe(clientId);
                                                channelClientMap.remove(ch);
                                            }

                                            ch.close();
                                            logger.info("channel closed after idle");
                                        }
                                    })
                                    .addLast(messageHandler);
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
