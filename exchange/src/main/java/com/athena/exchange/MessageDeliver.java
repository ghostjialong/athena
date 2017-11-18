package com.athena.exchange;

import com.athena.protobuf.MessageEntity;
import com.athena.client.ClientIdentity;
import com.athena.store.MessageStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
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

    private static Map<Long, ClientIdentity> clientMap = new HashMap();

    private final String EXCHANGE_UBICAST = "direct";
    private final String EXCHANGE_BROADCAST = "fanout";
    private final String EXCHANGE_PREFIX = "MESSAGE_BRLOKER_EXCHANGE_{0}";

    // 订阅单播私有消息
    public void subscribe(ClientIdentity clientIdentity) {
        clientMap.put(clientIdentity.getClientId(), clientIdentity);
        messageBroker.subscribe(MessageFormat.format(TOPIC_PREFIX,
                String.valueOf(clientIdentity.getClientId())));
    }

    // 订阅组播群组消息
    public void subscribe(ClientIdentity clientIdentity, int groupId) {
        clientMap.put(clientIdentity.getClientId(), clientIdentity);
        messageBroker.subscribe(MessageFormat.format(TOPIC_PREFIX,
                String.valueOf(clientIdentity.getClientId())), groupId);
    }

    public void pubMessage(MessageEntity.Message message) {
        // 校验一下消息格式
        Long destination = message.getRecipientId();
        messageBroker.pubMessage(MessageFormat.format(TOPIC_PREFIX, String.valueOf(destination)),
                message);
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
        messageStore.sinkData(message);

        //2. 消息投递，
        ClientIdentity clientIdentity = clientMap.get(message.getRecipientId());

        if (clientIdentity != null) {
            // 消息投递， 无论成败， 鸵鸟原则
            try {
                clientIdentity.consumeMessage(message);
            } catch (Exception e) {
                // 如果长连接断了， 消息投递失败， 此处鸵鸟不做处理， 因为消息已经做过持久存储， 持久存储的消息， ack  之后才被标记为
                // 已读或者 直接删掉
            }

        }
        logger.info("data got here!");
        //3. 收到ack 确认消息， 执行消息确认删除

    }
}
