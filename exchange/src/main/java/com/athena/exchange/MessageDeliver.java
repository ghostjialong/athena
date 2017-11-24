package com.athena.exchange;

import com.athena.protobuf.MessageEntity;
import com.athena.client.ClientIdentity;
import com.athena.store.MessageStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static Map<Integer, List<ClientIdentity>> clientGroupMap= new HashMap();

    private final String EXCHANGE_UBICAST = "direct";
    private final String EXCHANGE_BROADCAST = "fanout";
    private final String EXCHANGE_PREFIX = "MESSAGE_BRLOKER_EXCHANGE_{0}";

    // 订阅单播私有消息
    public void subscribe(ClientIdentity clientIdentity) {
        clientMap.put(clientIdentity.getClientId(), clientIdentity);
        // 生成客户端消息队列
        String queueName = MessageFormat.format(CLIENT_QUEUE_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        String topic = MessageFormat.format(TOPIC_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        messageBroker.subscribe(queueName, topic);
    }

    // 订阅组播群组消息
    public void subscribe(ClientIdentity clientIdentity, int groupId) {
        // clientMap.put(clientIdentity.getClientId(), clientIdentity);
        logger.info("client begin to subscribe.....");
        List<ClientIdentity> clients = clientGroupMap.get(groupId);
        if (clients == null ) {
            System.out.println("aaaaaaaaaaaa");
            clients = new ArrayList<ClientIdentity>();
            clients.add(clientIdentity);
        } else {
            clients.add(clientIdentity);
        }
        clientGroupMap.put(groupId, clients);

        String queueName = MessageFormat.format(CLIENT_QUEUE_PREFIX,
                String.valueOf(clientIdentity.getClientId()));
        String topic = MessageFormat.format(TOPIC_Group_PREFIX,
                String.valueOf(groupId));
        messageBroker.subscribe(queueName, topic);
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

    public void deliverData(MessageEntity.Message message) {
        //1. 消息入缓存队列存储，
        // messageStore.sinkData(message);
        logger.info("test data deliverd here");
        //2. 消息投递，
        if (message.getType().equals(MessageEntity.messageType.TO_GROUP) ) {
            List<ClientIdentity> clients = clientGroupMap.get(message.getGroupId());
            for (ClientIdentity clientIdentity : clients) {
                logger.info("enter loop......");
                pushMessage(message, clientIdentity);
            }
        } else {
            ClientIdentity clientIdentity = clientMap.get(message.getRecipientId());
            pushMessage(message, clientIdentity);
        }
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
}
