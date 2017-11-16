package com.athena.protobuf;

/**
 * Created by wangjialong on 11/6/17.
 */
public class ProtobufTest {

    public static void main(String[] args) throws Exception{
        MessageEntity.Message message = MessageEntity.Message.newBuilder()
                .setMessageId(105001676)
                .setRecipientId(105002955)
                .setBody("Hello, I wanna chat with you.....").build();
        //将对象转译成字节数组,序列化
        byte[] messageByteArray = message.toByteArray();
        //将字节数组转译成对象,反序列化
        MessageEntity.Message revceivedMsg = MessageEntity.Message.parseFrom(messageByteArray);

        System.out.println(message.getMessageId());
        System.out.println(messageByteArray);
        System.out.println(revceivedMsg.toString());
    }

}
