package com.athena.store;

import com.athena.protobuf.MessageEntity;

/**
 * Created by wangjialong on 11/12/17.
 */
public interface MessageStore {

    public void sinkData(MessageEntity.Message message) ;

    public void deliverData(MessageEntity.Message message);
}
