package com.athena.store;

import com.athena.protobuf.MessageEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by wangjialong on 11/12/17.
 */
@Component
public class MemoryStoreWithBuffer implements MessageStore {

    private Map<Integer, byte[][]> storeBuffer = new HashMap<>();
    private int bufferSize = 100;

    private static final long INIT_SQEUENCE = -1;

    // 记录下put/get/ack操作的三个下标
    private AtomicLong putSequence   = new AtomicLong(INIT_SQEUENCE); // 代表当前put操作最后一次写操作发生的位置
    private AtomicLong        getSequence   = new AtomicLong(INIT_SQEUENCE); // 代表当前get操作读取的最后一条的位置
    private AtomicLong        ackSequence   = new AtomicLong(INIT_SQEUENCE); // 代表当前ack操作的最后一条的位置

    public void deliverData(MessageEntity.Message message) {
        // 1. check 接收队列是否有空位， 如果接收队列满了, 即离线消息超过bufferSize 条， LRU 剔除最老消息， 如果想全部存储， 可以考虑mongoDB 存储
        // 内存模式只保留最近的bufferSize条消息
        long receiveId = message.getRecipientId();
        checkAndPut(receiveId, message.toByteArray());
    }

    public void sinkData(MessageEntity.Message message) {

    }

    private void checkAndPut(long clientId, byte[] body) {
        byte[][] buffer = storeBuffer.get(clientId);
        if (buffer == null) {
            buffer = new byte[bufferSize][];
        }
        long current = (putSequence.get() +1 ) % (bufferSize - 1)  ;
        int position = (int) current;
        buffer[position] = body;
        putSequence.getAndAdd(1);
        if ( current > ackSequence.get() % (bufferSize - 1)) {
            // 当前写入位置 超过了上次ack 的位置， 客户端长期不ack 消息， 新的消息冲进来， 需要覆盖部分未被ack的消息
            ackSequence.set(putSequence.get());
        }
        buffer[position] = body;
    }

    public List<MessageEntity.Message> getMessageByBatch(long clientId, int batchSize) {
        if (batchSize > bufferSize) {
            batchSize = bufferSize;
        }
        List<MessageEntity.Message> msgList = new ArrayList<MessageEntity.Message>();
        byte[][] buffer = storeBuffer.get(clientId);
        if (buffer == null || buffer.length <= 0) {
            return msgList;
        }
        long current = getSequence.get();
        long maxGet  = putSequence.get();
        long next = current;
        long end = current;
        end = (next + batchSize - 1) < maxGet ? (next + batchSize - 1) : maxGet;
        for(; next <= end; ++next) {
            byte[] msg = buffer[(int)next];
            try {
                msgList.add(MessageEntity.Message.parseFrom(msg));
                getSequence.getAndAdd(1);
            } catch(Exception e) {

            }

        }
        return msgList;
    }


}
