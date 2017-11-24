package com.athena.store;

import com.athena.protobuf.MessageEntity;
import com.athena.store.driver.MongoConnector;
import com.athena.store.pojo.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wangjialong on 11/19/17.
 */
@Component
public class MongoMessageStore implements MessageStore {

    static ObjectMapper mapper = new ObjectMapper();

    static String collection = "message_entities";

    @Autowired
    MongoConnector mongoConnector;

    // AOP 定义前置操作， 做数据清洗

    public void sinkData(MessageEntity.Message message) {
        try {
            Message messageOj = new Message(message.getSenderId(),
                    message.getRecipientId(), message.getMessageId(),
                    message.getType().getNumber(), message.getGroupId());
            String messageEntity = mapper.writeValueAsString(messageOj);
            Document document = Document.parse(messageEntity);
            mongoConnector.insertOne(collection, document);
        } catch (JsonProcessingException e) {

        }
    }
}
