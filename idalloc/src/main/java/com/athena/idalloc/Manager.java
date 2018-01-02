package com.athena.idalloc;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangjialong on 12/6/17.
 */
@Service
public class Manager implements java.io.Serializable{

    private AtomicInteger lastAllocId = new AtomicInteger(1);

    public void syncLastId() {

    }

    //
    public Long idAlloc() {
        Long idObtained = Long.valueOf(lastAllocId.getAndAdd(1));
        // 落地更新上次分配到的id， 用zookeerper 算了， 没测分配完同步分配id 到zookeerper

        return idObtained;
    }

    public void start() {
        // 启动定时线程， 定时同步分配的id
    }


}
