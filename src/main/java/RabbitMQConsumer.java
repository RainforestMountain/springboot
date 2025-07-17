import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer {
    // 主交换机和队列配置
    public static final String EXCHANGE_NAME = "DirectExchange";
    public static final String QUEUE_NAME = "DirectQueue";
    public static final String ROUTING = "DirectRouting";

    // 死信交换机和队列配置
    public static final String DLX_EXCHANGE_NAME = "DlxDirectExchange";
    public static final String DLX_QUEUE_NAME = "DlxDirectQueue";
    public static final String DLX_ROUTING = "DlxDirectRouting";

    // 云服务地址
    private static final String HOST = "111.229.164.75";
    // 默认端口号
    private static final int PORT = 5672;
    // 登录名
    private static final String USERNAME = "admin";
    // 密码
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 声明死信交换机和队列
        channel.exchangeDeclare(DLX_EXCHANGE_NAME, "direct", true);
        channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);
        channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, DLX_ROUTING);

        // 设置主队列的死信交换机参数
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        argsMap.put("x-dead-letter-routing-key", DLX_ROUTING);

        // 声明主交换机和队列
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, argsMap);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING);

        System.out.println(" [*] 等待DirectExchange中路由键为'" + ROUTING + "'的消息...");
        System.out.println(" [*] 死信队列配置: " + DLX_QUEUE_NAME + " 绑定到 " + DLX_EXCHANGE_NAME);

        // 创建消费者并监听队列
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] 收到消息: '" + message + "'");

            // 模拟处理失败，消息将被拒绝并发送到死信队列
            if (message.contains("error")) {
                System.out.println(" [!] 处理失败，消息将被发送到死信队列");
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
            } else {

                // 处理成功，确认消息
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println(" [✓] 消息处理成功");
            }
        };

        // 启动消费者，手动确认消息
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}