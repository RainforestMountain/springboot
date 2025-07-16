package com.example.lotterysystem.service.mq;

import com.example.lotterysystem.common.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.*;

/**
 * 死信队列 消费者
 * 用于消费正常队列消费失败的消息
 */
@Slf4j
@Component
@RabbitListener(queues = DLX_QUEUE_NAME)
public class DlxReceiver {
    private static final Logger logger =
            LoggerFactory.getLogger(MqReceiver.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void process(Map<String, String> message) {
        //打印json字符串
        logger.info("开始处理异常消息！message:{}",
                JacksonUtil.writeValueAsString(message));
        //消息message通过通道重新发送到正常交换机, 正常队列
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING, message);
    }
}
