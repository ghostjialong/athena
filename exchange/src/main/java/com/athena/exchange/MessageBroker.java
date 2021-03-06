package com.athena.exchange;

import com.athena.exchange.driver.MessageQueue;
import com.athena.protobuf.MessageEntity;
import com.athena.store.MessageStore;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

    private Logger logger = Logger.getLogger("MessageBroker.class");
    private Map<String, List<MessageEntity.Message>> storeBuffer = new HashMap<>();

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public void connect() throws Exception {
        brokerConnector.connect();
    }

    public void start() {
        try {
            brokerConnector.setMessageBroker(this);
            brokerConnector.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void subscribe(String queue, String topic) {
        try {
            Consumer consumer = brokerConnector.subscribe(queue, topic);
            MessageDeliver.registerConsumer(queue, consumer);
        } catch (IOException e) {
            brokerConnector.reconnect();
        }
    }


    public void unSubscribe(String topic, String clientId) throws Exception {
        // 删除broker 订阅队列
        brokerConnector.deleteQueue(topic);
        // 释放consumer 对象

    }

    public void pubMessage(String topic, MessageEntity.Message message) {
        logger.info("fafafafafaf : " + message.getMessageId());
        byte[] byteMessage = message.toByteArray();
        System.out.println("fuck test here....");
        try {
            brokerConnector.pubMessage(topic, byteMessage);
        } catch (IOException e) {
            // 连接重连
            e.printStackTrace();
            brokerConnector.reconnect();
        }

    }

    public void getMessageWithOutAck(byte[] body, long deliverTag, Consumer consumer) {
        // 获取订阅的topic 对应的消息, 将消息投递至业务线程池
        try {
            MessageEntity.Message message = MessageEntity.Message.parseFrom(body);
            Thread th = new Thread(()->{
                try {
                    messageDeliver.deliverData(message, consumer);
                    brokerConnector.ackMessage(deliverTag);
                } catch (IOException e) {

                }

            });
            th.start();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }
}
