package com.athena.client;

import io.netty.channel.ChannelFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by wangjialong on 12/9/17.
 */
public class TestClient {

    private NettyClient nettyClient;
    private ChannelFuture future;

    @Before
    public void setup() {
        nettyClient = new NettyClient("127.0.0.1", 8069);
        nettyClient.start();
    }

    @Test
    public void sendMessage() throws InterruptedException {
        //nettyClient.sendMessage();
        //future.channel().closeFuture().sync();
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread th = new Thread(()-> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    break;
                }

            }
        });
        th.start();
        th.join();
    }

}
