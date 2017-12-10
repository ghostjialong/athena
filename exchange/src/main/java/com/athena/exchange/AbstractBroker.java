package com.athena.exchange;

import com.rabbitmq.client.Consumer;

/**
 * Created by wangjialong on 11/11/17.
 */
public interface AbstractBroker {

    public void connect() throws Exception;

    public void subscribe(String queue, String topic) throws Exception;

    public void unSubscribe(String topic, String clientId) throws Exception;

    public void getMessageWithOutAck(byte[] body, long deliverTag, Consumer consumer);
}
