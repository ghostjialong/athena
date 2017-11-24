package com.athena.client;

import com.athena.protobuf.MessageEntity;
import com.athena.protobuf.RequestEntity;
import com.athena.protobuf.ResponseEntity;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import sun.tools.tree.CastExpression;

import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */
public class NettyClient {

    private int port;
    private String address;

    private Logger logger = Logger.getLogger("NettyClient.class");

    private Object lock = new Object();

    public NettyClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtobufEncoder())
                                    .addLast("init_handler", new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            ByteBuf byteBuf = (ByteBuf) msg;
                                            try {

                                                byte[] body = new byte[byteBuf.readableBytes()];
                                                byteBuf.getBytes(0, body);
                                                MessageEntity.Message message = MessageEntity.Message.parseFrom(body);

                                                ctx.channel().pipeline().addAfter("init_handler", "msg_decode_handler",
                                                        new ProtobufDecoder(MessageEntity.Message.getDefaultInstance()));
                                                logger.info("add msg_decode_handler");
                                            } catch (InvalidProtocolBufferException e) {
                                                ctx.channel().pipeline().addAfter("init_handler", "response_decode_handler",
                                                        new ProtobufDecoder(ResponseEntity.Response.getDefaultInstance()));
                                                logger.info("add response_decode_handler");
                                            }
                                            ctx.fireChannelRead(msg);
                                        }
                                    })
                                    //.addLast(new ProtobufDecoder(MessageEntity.Message.getDefaultInstance()))
                                    //.addLast(new ProtobufDecoder(ResponseEntity.Response.getDefaultInstance()))
                                    .addLast("client_handler", new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            ctx.pipeline().remove(ProtobufDecoder.class);
                                            if (msg instanceof  ResponseEntity.Response) {
                                                ResponseEntity.Response message = (ResponseEntity.Response) msg;
                                                logger.info("client got the message " + message.toString());
                                            } else {
                                                MessageEntity.Message message = (MessageEntity.Message) msg;
                                                logger.info("client got the message " + message.toString());
                                            }

                                            Channel channel = ctx.channel();
                                            synchronized (channel) {
                                                channel.notify();
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            cause.getCause().printStackTrace();
                                        }

                                    });
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(address, port);
            future.addListener((ChannelFuture future2)->{
                System.out.println("connect to server......");

                RequestEntity.Request request = RequestEntity.Request.newBuilder()
                        .setRequestType(RequestEntity.PacketType.SUBSCRIBE)
                        .setAuthToken("MhxzKhl")
                        .setGroupId(566)
                        .setClientId(105001676).build();
                        //.setMessage(message).build();

                future2.channel().writeAndFlush(request).addListener((ChannelFuture thisFuture) -> {
                    Channel channel = thisFuture.channel();
                    logger.info("call back listener executed....");
                    Thread thread = new Thread(()->{
                        synchronized (channel) {
                            try {
                                channel.wait();
                                logger.info("I send a message....");
                                MessageEntity.Message message = MessageEntity.Message.newBuilder()
                                        .setMessageId(123456)
                                        //.setRecipientId(105001676)
                                        .setSenderId(105001676)
                                        .setType(MessageEntity.messageType.TO_GROUP)
                                        .setGroupId(566)
                                        .setBody("Hello, I wanna fuck with you.....").build();
                                RequestEntity.Request msg = RequestEntity.Request.newBuilder()
                                        .setRequestType(RequestEntity.PacketType.PUB)
                                        .setAuthToken("MhxzKhl")
                                        .setClientId(105001676)
                                        .setMessage(message).build();
                                //.setMessage(message).build();
                                channel.writeAndFlush(msg);
                            } catch (InterruptedException e) {

                            }

                        }
                    });
                    thread.start();
                });

                //future2.channel().writeAndFlush(message);
            });
            future.channel().closeFuture().sync();
        } catch(Exception e) {
            System.out.println(e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
