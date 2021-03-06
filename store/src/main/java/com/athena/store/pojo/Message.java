package com.athena.store.pojo;

/**
 * Created by wangjialong on 11/21/17.
 */
public class Message {

    public Long fromUserId;

    public Long toUserId;

    public Long messageId;

    public Long createTime;

    public int isAck;

    public Long ackTime;

    public int type = 0;

    public long groupId = -1;

    public String body = "";

    public Message() {

    }

    public Message(Long fromUserId, long toUserId, Long messageId,
                   int type, long groupId, String body ) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.messageId = messageId;
        this.type = type;
        this.groupId = groupId;
        this.createTime = System.currentTimeMillis() / 1000;
        this.ackTime = Long.valueOf(-1);
        this.body = body;
    }

    public static class Builder {

    }
}
