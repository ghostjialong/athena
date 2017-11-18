package com.athena.exchange.driver;

import com.athena.exchange.AbstractBroker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by wangjialong on 11/8/17.
 */
public interface MessageQueue {

    void connect() throws TimeoutException, IOException;

    void pubMessage(String topic, byte[] body) throws IOException;

    byte[] syncMessageGetSync(String queue) throws IOException;

    void register(String queue) throws IOException;

    void deleteQueue(String queueName) throws Exception;

    public void subscribe(String topic) throws IOException;

    public void subscribe(String topic, int groupId) throws IOException;

    void setMessageBroker(AbstractBroker messageBroker);

    public void ackMessage(long deliveryTag) throws IOException;

    public void reconnect();
}
