import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer {
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

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 声明队列（如果不存在则创建）
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] 等待消息中...");

        // 创建消费者并监听队列
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] 收到消息: '" + message + "'");
        };

        // 启动消费者，自动确认消息
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
