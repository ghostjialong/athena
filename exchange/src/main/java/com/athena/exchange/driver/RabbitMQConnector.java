package com.athena.exchange.driver;

import com.athena.exchange.AbstractBroker;
import com.rabbitmq.client.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;


/**
 * Created by wangjialong on 11/8/17.
 */
@Component
public class RabbitMQConnector implements MessageQueue{

    private static String address;
    private static int port;
    private static String username;
    private static String password;
    private static String exchange;
    private static String exchangeType;
    private ConnectionFactory connectionFactory;
    private Channel channel;
    private AbstractBroker messageBroker;
    private Connection connection;

    private static Logger logger = Logger.getLogger("RabbitMQConnector");
    private Map<String, Consumer> clientConsumers = new HashMap<>();

    static {
        try {
            String conf = System.getProperty("athena.conf", "classpath:athena.conf");
            logger.info("test conf name " + conf);
            Properties properties = new Properties();
            String configName = StringUtils.substringAfter(conf, "classpath:");
            properties.load(RabbitMQConnector.class.getClassLoader().getResourceAsStream(configName));
            address  = properties.getProperty("address");
            port     = Integer.valueOf(properties.getProperty("port"));
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            exchange = properties.getProperty("exchange");
            exchangeType = properties.getProperty("exchange_type");
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public void setMessageBroker(AbstractBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    public void connect() throws TimeoutException , IOException {
        connectionFactory = new ConnectionFactory();

        connectionFactory.setHost(address);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connection = connectionFactory.newConnection();
        channel = connectionFactory.newConnection().createChannel();
        channel.exchangeDeclare(exchange, exchangeType, true, false, null);
    }

    // 订阅私有消息
    public void subscribe(String queue, String topic) throws IOException {
        // 需要记录下订阅的topic, 方便与消息broker 重连后的订阅关系恢复
        // to be continued.......
        channel.queueDeclare(queue, false, false, false, null);
        channel.queueBind(queue, exchange, topic);
        register(queue);
    }

    public void deleteQueue(String queueName) throws Exception {
        channel.queueDelete(queueName);
    }

    public void pubMessage(String topic, byte[] body) throws IOException {
        publishMessage(channel, topic, exchange, body);
    }

    public void publishMessage(Channel ch, String routingKey, String exchange,
                              byte[] body ) throws IOException {
        ch.basicPublish(exchange, routingKey, false, null, body);
    }

    public void register(String queue) throws IOException {
        channel.basicConsume(queue, RabbitConsumerFactory.getConsumer(channel, messageBroker));
    }

    public byte[] syncMessageGetSync(String queue) throws IOException {
        channel.queueDeclare(queue, false, false, false, null);
        GetResponse resp = channel.basicGet(queue, true);
        if (resp == null) {
            byte[] data = new byte[1];
            return data;
        }
        return resp.getBody();
    }

    public Channel getChannel() {
        return channel;
    }

    public void ackMessage(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public void reconnect() {

    }

}
