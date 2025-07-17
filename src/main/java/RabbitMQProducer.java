import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducer {
    // 默认交换机名称
    private static final String EXCHANGE_NAME = "";
    // 默认队列名称
    private static final String QUEUE_NAME = "default_queue";
    // 云服务地址
    private static final String HOST = "111.229.164.75";
    // 默认端口号
    private static final int PORT = 5672;
    // 登录名
    private static final String USERNAME = "admin";
    // 密码
    private static final String PASSWORD = "*****";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明队列（如果不存在则创建）
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // 要发送的消息
            String message = "Hello, RabbitMQ!";

            // 发送消息到默认交换机，路由键为队列名称
            channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [x] 发送消息: '" + message + "'");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}    