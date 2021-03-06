package com.athena.exchange.driver;

import com.athena.exchange.AbstractBroker;
import com.rabbitmq.client.*;

/**
 * Created by wangjialong on 11/12/17.
 */
public class RabbitConsumerFactory {

    public static Consumer getConsumer(Channel ch, AbstractBroker messageBroker) {
        return new DefaultConsumer(ch) {

            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) {
                try {
                    messageBroker.getMessageWithOutAck(body, envelope.getDeliveryTag(), this);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
