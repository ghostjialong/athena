package com.athena.protobuf;

import java.lang.reflect.Field;

/**
 * Created by wangjialong on 11/6/17.
 */
public class ProtobufTest {

    public static void main(String[] args) throws Exception{
        MessageEntity.Message message = MessageEntity.Message.newBuilder()
                .setSenderId(105001676)
                .setRecipientId(105002955)
                .setType(MessageEntity.messageType.TO_GROUP)
                .setBody("Hello, I wanna chat with you.....").build();
        //将对象转译成字节数组,序列化
        Long messageId = Long.valueOf(1234);
        MessageEntity.Message message2 = addMessageId(message, messageId);
        byte[] messageByteArray = message2.toByteArray();
        //将字节数组转译成对象,反序列化
        MessageEntity.Message revceivedMsg = MessageEntity.Message.parseFrom(messageByteArray);

        System.out.println(message.getMessageId());
        System.out.println(messageByteArray);
        System.out.println(revceivedMsg.toString());
    }

    private static MessageEntity.Message addMessageId(MessageEntity.Message message, long messageId) {
        MessageEntity.Message messageWithId = MessageEntity.Message.newBuilder()
                .setMessageId(messageId)
                .setRecipientId(message.getRecipientId())
                .setGroupId(message.getGroupId())
                .setSenderId(message.getSenderId())
                .setBody(message.getBody())
                .setHeader(message.getHeader())
                .setType(message.getType())
                .build();
        return messageWithId;
    }

}
