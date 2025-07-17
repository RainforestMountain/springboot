import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducer {
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

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明主交换机（如果不存在）
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // 发送10条消息，其中包含"error"的消息会被拒绝并发送到死信队列
            for (int i = 1; i <= 10; i++) {
                String message = "Message " + i + (i % 3 == 0 ? " error" : "");
                channel.basicPublish(EXCHANGE_NAME, ROUTING, null, message.getBytes());
                System.out.println(" [x] 发送消息: '" + message + "'");
                Thread.sleep(500);
            }

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}