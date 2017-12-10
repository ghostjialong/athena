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

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/7/17.
 */
public class NettyClient {

    private int port;
    private String address;

    private Logger logger = Logger.getLogger("NettyClient.class");
    private CountDownLatch latch = new CountDownLatch(1);

    private Object lock = new Object();

    private long clientId;
    private RequestClient requestClient;
    private Channel channel;

    public NettyClient(String address, int port) {
        this.address = address;
        this.port = port;
        requestClient = new RequestClient();
        clientId = 105008676;
    }

    public NettyClient() {
        Random random = new Random();
        int max = 19999;
        int min = 10000;

        int s = random.nextInt(max)%(max-min+1) + min;
        clientId = (long) s;
    }

    public void start() {
        this.connect();
    }

    public ChannelFuture connect() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(10);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtobufEncoder())
                                    .addLast("init_handler", getHandler())
                                    .addLast("client_handler", getMessageHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(address, port);
            future.addListener((ChannelFuture future2)-> {
                System.out.println("connect to server......");
                channel = future2.channel();
                //future2.channel().writeAndFlush(request).addListener((ChannelFuture thisFuture) -> {
                //    System.out.println(thisFuture.isSuccess());
                //});
                Thread th = new Thread(()->{
                    subscribe();
                    sendMessage();
                });
                th.start();
                latch.countDown();
            });
            latch.await();
            future.channel().closeFuture().sync();
            return future;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            //eventLoopGroup.shutdownGracefully();
        }
        return null;
    }

    private void subscribe() {
        RequestEntity.Request request = RequestEntity.Request.newBuilder()
                .setRequestType(RequestEntity.PacketType.SUBSCRIBE)
                .setRequestId(UUID.randomUUID().toString())
                .setAuthToken("MhxzKhl")
                .setGroupId(566)
                .setClientId(clientId).build();
        //channel.writeAndFlush(request);
        ResponseEntity.Response response = requestClient.call(channel, request);
        logger.info("response: " + response.toString());
    }

    public void sendMessage() {
        logger.info("I send a message....");
        MessageEntity.Message message = MessageEntity.Message.newBuilder()
                .setType(MessageEntity.messageType.TO_GROUP)
                .setGroupId(566)
                .setSenderId(clientId)
                .setBody("Hello, I wanna fuck with you.....").build();
        RequestEntity.Request msg = RequestEntity.Request.newBuilder()
                .setRequestType(RequestEntity.PacketType.PUB)
                .setAuthToken("MhxzKhl")
                .setClientId(clientId)
                .setMessage(message).build();
        requestClient.cast(channel, msg);
    }

    private ChannelInboundHandlerAdapter getMessageHandler() {
        return new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ctx.pipeline().remove(ProtobufDecoder.class);
                if (msg instanceof  ResponseEntity.Response) {
                    ResponseEntity.Response message = (ResponseEntity.Response) msg;
                    logger.info("client got the message " + message.toString());
                    SyncFuture future = RequestClient.getSyncFuture(message.getRequestId());
                    logger.info("sync future: " + future.toString());
                    if (future != null) {
                        future.setResponse(message);
                        RequestClient.getSyncFuture(message.getRequestId());
                        //RequestClient.removeSyncFuture(message.getRequestId());
                    }
                    // 取响应的requestId,
                } else {
                    MessageEntity.Message message = (MessageEntity.Message) msg;
                    logger.info("client got the message here..... " + message.toString());
                    RequestEntity.Request request = RequestEntity.Request.newBuilder()
                            .setRequestType(RequestEntity.PacketType.ACK)
                            .setAuthToken("MhxzKhl")
                            .setMessageId(message.getMessageId())
                            .setClientId(105001676).build();
                    //.setMessage(message).build();
                    ctx.channel().writeAndFlush(request);
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

        };
    }

    private ChannelInboundHandlerAdapter getHandler() {
        return new ChannelInboundHandlerAdapter() {
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
        };
    }
}
