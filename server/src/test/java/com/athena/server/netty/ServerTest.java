package com.athena.server.netty;

import com.athena.client.NettyClient;

/**
 * Created by wangjialong on 11/7/17.
 */
public class ServerTest {

    public void testNettyProtobufClient() {
        // start server
        final NettyServer testServer = new NettyServer();
        new Thread(()->{
            testServer.start();
        }).start();
        NettyClient testClient = new NettyClient("127.0.0.1", 8069);
        testClient.connect();
    }

    public static void main(String[] args) {
        ServerTest testSuit = new ServerTest();
        testSuit.testNettyProtobufClient();
    }
}
