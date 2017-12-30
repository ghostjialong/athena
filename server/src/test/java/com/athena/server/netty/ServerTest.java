package com.athena.server.netty;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.athena.exchange.MessageBroker;
import com.athena.idalloc.client.IdallocClient;
import com.athena.server.netty.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wangjialong on 11/7/17.
 */
public class ServerTest {

    private static ApplicationContext ctx;

    @Autowired
    public IdallocClient idallocClient;

    static {
        ctx = new ClassPathXmlApplicationContext(
                "classpath:instance-spring.xml");
        //ctx.start();
    }

    public void testDubboConsume() {
        GenericService service = (GenericService) ctx.getBean("idalloc");
        GenericService service2 = (GenericService) ctx.getBean("idalloc");
        Object result = service.$invoke("idAllocForRequest", new String[] {"java.lang.String"},
                new Object[] {"MhxzKhl"});
        System.out.println("fuck miaolinjie...." + String.valueOf(result));

    }

    public static void main(String[] args) {
        MessageHandler messageHandler = (MessageHandler) ctx.getBean("messageHandler");
        Long id = messageHandler.generateMessageId();
        System.out.println("fafaf" + String.valueOf(id));
    }

    public void testNettyProtobufClient() {
        // start server
        final NettyServer testServer = (NettyServer) ctx.getBean("nettyServer", NettyServer.class);
        //final NettyServer testServer = new NettyServer();
        new Thread(()->{
            testServer.start();
        }).start();
        //NettyClient testClient = new NettyClient("127.0.0.1", 8069);
        //testClient.connect();
    }

    public static void main2(String[] args) {
        MessageBroker messageBroker = (MessageBroker) ctx.getBean("message_broker", MessageBroker.class);
        messageBroker.start();
        ServerTest testSuit = new ServerTest();
        testSuit.testNettyProtobufClient();
    }
}
