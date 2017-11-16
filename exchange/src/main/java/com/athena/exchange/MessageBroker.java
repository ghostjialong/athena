package com.athena.exchange;

import com.athena.exchange.driver.MessageQueue;
import com.athena.exchange.driver.RabbitMQConnector;
import com.athena.protobuf.MessageEntity;
import com.athena.store.MessageStore;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangjialong on 11/8/17.
 */
@Component("message_broker")
public class MessageBroker implements AbstractBroker {

    @Autowired
    private MessageQueue brokerConnector;

    private MessageStore messageStore;

    @Autowired
    private MessageDeliver messageDeliver;

    private Map<String, List<MessageEntity.Message>> storeBuffer = new HashMap<>();

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public void connect() throws Exception {
        brokerConnector.connect();
        brokerConnector.preExecute("wangjialong", "hiclub_message");
    }

    public void start() {
        try {
            brokerConnector.setMessageBroker(this);
            brokerConnector.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void subscribe(String topic) throws Exception {
        brokerConnector.subscribe(topic);
    }

    public void unSubscribe(String topic, String clientId) throws Exception {
        // 删除broker 订阅队列
        brokerConnector.deleteQueue(topic);
        // 释放consumer 对象

    }

    public void pubMessage(String topic, MessageEntity.Message message) {
        byte[] byteMessage = message.toByteArray();
        try {
            brokerConnector.pubMessage(topic, byteMessage);
        } catch (IOException e) {
            // 连接重连
            brokerConnector.reconnect();
        }

    }

    public void getMessageWithOutAck(byte[] body, long deliverTag) {
        // 获取订阅的topic 对应的消息, 将消息投递至业务线程池
        try {
            MessageEntity.Message message = MessageEntity.Message.parseFrom(body);
            Thread th = new Thread(()->{
                try {
                    messageDeliver.deliverData(message);
                    brokerConnector.ackMessage(deliverTag);
                } catch (IOException e) {

                }

            });
            th.start();
        } catch (InvalidProtocolBufferException e) {

        }

    }
}
