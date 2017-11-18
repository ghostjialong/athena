package com.athena.exchange.driver;

import com.athena.exchange.MessageBroker;
import com.rabbitmq.client.Channel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by wangjialong on 11/9/17.
 */
public class TestRabbitMQConnector {

    RabbitMQConnector rabbitMQConnector;

    @Before
    public void setUp() throws Exception {
        rabbitMQConnector = getRabbitMQConnector();
        rabbitMQConnector.connect();
        Channel channel = rabbitMQConnector.getChannel();
        channel.queueDeclare("wangjialong", false, false, false, null);
        channel.queueBind("wangjialong", "message_broker", "wangjialong");
    }

    @Test
    public void testRabbitMQPubMsg() throws Exception {
        String message = "unit test message";
        rabbitMQConnector.pubMessage("wangjialong", message.getBytes());
    }

    private RabbitMQConnector getRabbitMQConnector() throws Exception {
        RabbitMQConnector rabbitMQConnector = new RabbitMQConnector();
        rabbitMQConnector.connect();
        return rabbitMQConnector;
    }

    @Test
    public void testRabbitMQConsumeMsgPull() throws Exception {
        String message2 = "unit test message";
        rabbitMQConnector.pubMessage("wangjialong", message2.getBytes());
        byte[] body = rabbitMQConnector.syncMessageGetSync("wangjialong");
        String message = new String(body);
        Assert.assertEquals("unit test message", message);
    }

    @Test
    public void testRabbitMQConsumePush() throws Exception {
        for (int i = 0; i< 10; i++) {
            Thread th = new Thread(()-> {
                try {
                    RabbitMQConnector rabbitMQConnector = getRabbitMQConnector();
                    rabbitMQConnector.setMessageBroker(new MessageBroker());
                    rabbitMQConnector.register("wangjialong");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            th.start();
        }
    }

    @After
    public void cleanup() throws IOException {
        Channel channel = rabbitMQConnector.getChannel();
        channel.queueDelete("wangjialong");
    }
}
