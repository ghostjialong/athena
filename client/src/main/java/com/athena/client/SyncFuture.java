package com.athena.client;

import com.athena.protobuf.ResponseEntity;

import java.util.concurrent.*;

/**
 * Created by wangjialong on 12/10/17.
 */
public class SyncFuture implements Future<ResponseEntity.Response> {

    private CountDownLatch latch = new CountDownLatch(1);
    private String requestId;
    private ResponseEntity.Response response;

    public SyncFuture(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public void setResponse(ResponseEntity.Response response) {
        this.response = response;
        latch.countDown();
    }

    @Override
    public ResponseEntity.Response get() throws InterruptedException, ExecutionException {
        latch.await();
        return response;
    }

    @Override
    public ResponseEntity.Response get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (latch.await(timeout, unit)) {

            return response;
        }
        return null;
    }
}
