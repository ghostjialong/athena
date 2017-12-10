package com.athena.client;

import com.athena.protobuf.RequestEntity;
import com.athena.protobuf.ResponseEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by wangjialong on 12/9/17.
 */
public class RequestClient {

    private static Map<String, SyncFuture> requestMap = new HashMap();
    private Logger logger = Logger.getLogger("RequestClient.class");

    public static SyncFuture getSyncFuture(String requestId) {
        return requestMap.get(requestId);
    }

    public static void removeSyncFuture(String requestId) {
        SyncFuture future = requestMap.remove(requestId);
        future = null;
    }

    // rpc 同步调用
    public ResponseEntity.Response call(Channel channel, RequestEntity.Request request) {
        // 生成同步Future
        SyncFuture syncFuture = new SyncFuture(request.getRequestId());
        requestMap.put(request.getRequestId(), syncFuture);
        logger.info("test sync : " + syncFuture);
        logger.info("put into map, requestId: " + request.getRequestId());
        channel.writeAndFlush(request).addListener((ChannelFuture thisFuture) -> {
            // 消息成功出去应该做什么，
            if (! thisFuture.isSuccess()) {
                // 发送请求失败， 抛异常吧
                throw new RuntimeException();
            }
        });
        ResponseEntity.Response response = null;
        try {
            response = syncFuture.get(5, TimeUnit.SECONDS);
            removeSyncFuture(request.getRequestId());
            if(response != null ) {
                return response;
            } else {
                response = ResponseEntity.Response.newBuilder()
                        .setCode("504")
                        .setMessage("request timeout").build();
                return response;
            }
        } catch (Exception e) {
            // 构造一个超时响应
            e.printStackTrace();
        } finally {

        }
        return response;
    }

    // rpc 异步调用
    public void cast(Channel channel, RequestEntity.Request request) {
        try {
            channel.writeAndFlush(request).addListener((ChannelFuture thisFuture) -> {
                // 消息成功出去应该做什么，
                if (! thisFuture.isSuccess()) {
                    // 发送请求失败， 抛异常吧
                    thisFuture.cause().printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
