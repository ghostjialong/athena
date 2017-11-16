package com.athena.exchange.driver;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wangjialong on 11/9/17.
 */
public class TestRabbitMQConnector {

    @Test
    public void testRabbitMQPubMsg() throws Exception {
        RabbitMQConnector rabbitMQConnector = getRabbitMQConnector();
        String message = "test fuck 123";
        rabbitMQConnector.pubMessage("wangjialong", message.getBytes());
    }

    private RabbitMQConnector getRabbitMQConnector() throws Exception {
        RabbitMQConnector rabbitMQConnector = new RabbitMQConnector();
        rabbitMQConnector.connect();
        rabbitMQConnector.preExecute("wangjialong", "hiclub_message");
        return rabbitMQConnector;
    }

    @Test
    public void testRabbitMQConsumeMsgPull() throws Exception {
        RabbitMQConnector rabbitMQConnector = getRabbitMQConnector();
        byte[] body = rabbitMQConnector.syncMessageGetSync("wangjialong");
        String message = new String(body);
        Assert.assertEquals("test fuck 123", message);
    }

    @Test
    public void testRabbitMQConsumePush() throws Exception {
        testRabbitMQPubMsg();
        for (int i = 0; i< 10; i++) {
            Thread th = new Thread(()-> {
                try {
                    System.out.println("ggggg");
                    RabbitMQConnector rabbitMQConnector = getRabbitMQConnector();
                    rabbitMQConnector.consumeMsgAsync("wangjialong");
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
            th.start();
        }
        Thread.sleep(10000);
    }
}
