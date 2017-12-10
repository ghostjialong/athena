package com.athena.server.netty;

import com.athena.client.NettyClient;
import com.athena.exchange.MessageBroker;
import com.athena.exchange.driver.MessageQueue;
import com.athena.exchange.driver.RabbitMQConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wangjialong on 11/7/17.
 */
public class ServerTest {

    private static ApplicationContext ctx;

    static {
        ctx = new ClassPathXmlApplicationContext(
                "classpath:instance-spring.xml");
    }



    public void testNettyProtobufClient() {
        // start server
        final NettyServer testServer = ctx.getBean("nettyServer", NettyServer.class);
        //final NettyServer testServer = new NettyServer();
        new Thread(()->{
            testServer.start();
        }).start();
        //NettyClient testClient = new NettyClient("127.0.0.1", 8069);
        //testClient.connect();
    }

    public static void main(String[] args) {
        MessageBroker messageBroker = ctx.getBean("message_broker", MessageBroker.class);
        messageBroker.start();
        ServerTest testSuit = new ServerTest();
        testSuit.testNettyProtobufClient();
    }
}
