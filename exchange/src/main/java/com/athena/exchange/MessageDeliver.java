package com.athena.exchange;

import com.athena.protobuf.MessageEntity;
import com.athena.client.ClientIdentity;
import com.athena.store.MessageStore;
import com.rabbitmq.client.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by wangjialong on 11/13/17.
 */

@Component
public class MessageDeliver {

    @Autowired
    private MessageStore messageStore;

    private static Logger logger = Logger.getLogger("MessageDeliver.class");

    @Autowired
    public MessageBroker messageBroker;

    private static final String TOPIC_PREFIX = "Athena_Topic_{0}";

    private static final String CLIENT_QUEUE_PREFIX = "Athena_Queue_{0}";

    private static final String TOPIC_Group_PREFIX = "Athena_Topic_Group_{0}";

    private static Map<Long, ClientIdentity> clientMap = new HashMap();

    private static Map<Consumer, String> consumerMap = new HashMap();
    private final String EXCHANGE_UBICAST = "direct";
    private final String EXCHANGE_BROADCAST = "fanout";
    private final String EXCHANGE_PREFIX = "MESSAGE_BRLOKER_EXCHANGE_{0}";

    public boolean isSubscribed(ClientIdentity clientIdentity) {
        return clientMap.containsKey(clientIdentity.getClientId());
    }

    // 订阅单播私有消息
    public void subscribe(ClientIdentity clientIdentity) {
        // 检查下是否之前订阅过
        if (isSubscribed(clientIdentity)) {
            clientMap.put(clientIdentity.getClientId(), clientIdentity);
            return;
        }
        // 生成客户端消息队列
        clientMap.put(clientIdentity.getClientId(), clientIdentity);
        String topic = MessageFormat.format(TOPIC_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        String queueName = MessageFormat.format(CLIENT_QUEUE_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        messageBroker.subscribe(queueName, topic);
    }

    // 订阅组播群组消息
    public void subscribe(ClientIdentity clientIdentity, int groupId) {
        // 检查下是否之前订阅过
        if (isSubscribed(clientIdentity)) {
            clientMap.put(clientIdentity.getClientId(), clientIdentity);
            return;
        }
        // 生成客户端消息队列
        clientMap.put(clientIdentity.getClientId(), clientIdentity);
        String queueName = MessageFormat.format(CLIENT_QUEUE_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        String topic = MessageFormat.format(TOPIC_Group_PREFIX,
                String.valueOf(groupId));
        messageBroker.subscribe(queueName, topic);
    }

    public static void registerConsumer(String queueName, Consumer consumer) {
        consumerMap.put(consumer, queueName);
    }

    public void pubMessage(MessageEntity.Message message) {
        // 校验一下消息格式
        Long destination = message.getRecipientId();
        messageBroker.pubMessage(MessageFormat.format(TOPIC_PREFIX, String.valueOf(destination)),
                message);
    }

    public void pubMessage(MessageEntity.Message message, int groupId) {
        String topic = MessageFormat.format(TOPIC_Group_PREFIX,
                String.valueOf(groupId));
        // 这里需要生成消息的messageId
        messageBroker.pubMessage(topic, message);
    }

    public static void unSubscribe(long clientId) {
        ClientIdentity clientIdentity = clientMap.get(clientId);
        if (clientIdentity !=null ) {
            clientMap.remove(clientId);
            // 释放对象，交给GC 处理
            clientIdentity = null;
        }
    }

    public void deliverData(MessageEntity.Message message, Consumer consumer) {
        //1. 消息入缓存队列存储，
        // messageStore.sinkData(message);
        logger.info("test data deliverd here");
        logger.info("caonima: " + message.getMessageId());
        String queueName = consumerMap.get(consumer);
        Long clientId = Long.valueOf(queueName.substring(queueName.lastIndexOf("_") + 1));
        ClientIdentity clientIdentity = clientMap.get(clientId);
        pushMessage(message, clientIdentity);
        logger.info("data got here!");
        //3. 收到ack 确认消息， 执行消息确认删除

    }

    private void pushMessage(MessageEntity.Message message, ClientIdentity clientIdentity) {
        if (clientIdentity != null) {
            // 消息投递， 无论成败， 鸵鸟原则
            try {
                messageStore.sinkData(message);
                clientIdentity.consumeMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                // 如果长连接断了， 消息投递失败， 此处鸵鸟不做处理， 因为消息已经做过持久存储， 持久存储的消息， ack  之后才被标记为
                // 已读或者 直接删掉
            }

        }
    }

    public void ackMessage(Long messageId) {
        messageStore.ackMessage(messageId);
    }
}
