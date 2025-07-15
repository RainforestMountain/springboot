package com.example.lotterysystem.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
//DirectRabbitConfig 配置类, 配置mq的组件和相关属性
/**
 * 回顾架构图里的核心模块：
 *
 * Producer（生产者）：图里的 Producer1、Producer2，负责发消息。
 * Connection（连接）：客户端（生产者 / 消费者）和 Broker（RabbitMQ Server）建立的 TCP 长连接，
 * 代码里没直接体现创建，由 Spring AMQP 底层管理。
 * Channel（信道）：基于 Connection 的轻量级通道，实际收发消息走它，
 * 代码也没显式创建，框架按需维护。
 * Broker（RabbitMQ Server）：整个中间件服务，提供消息路由、存储等能力，
 * 代码里的配置会在 Broker 上创建队列、交换机等。
 * Virtual Host（虚拟主机）：逻辑隔离的 “小 Broker”，代码没单独配置（默认用 / ），
 * 可通过配置指定，用于隔离不同业务的队列、交换机。
 * Exchange（交换机）：接收生产者消息，按规则路由到队列，
 * 代码里定义了 DirectExchange 类型的交换机。
 * Queue（队列）：存储消息，供消费者获取，代码里定义了业务队列和死信队列。
 * Consumer（消费者）：图里的 Consumer1、Consumer2 ，
 * 代码里需额外写监听逻辑（比如用 @RabbitListener ）来消费队列消息，当前配置类没包含这部分。
 * 普通交换机和队列、死信交换机和队列是否要分在不同虚拟机（Virtual Host），没有绝对的标准，需要综合多方面因素来考虑，以下是对不同选择的分析：
 *
 * 分在不同虚拟机的情况
 * 优点
 * 更强的隔离性：不同业务场景下，普通消息和死信消息的重要性、处理逻辑以及访问权限都可能不同。
 * 将它们放置在不同的虚拟机中，能实现更严格的资源隔离。例如，金融类应用中，普通业务消息可能涉及正常的交易流程，而死信消息可能是交易失败、异常退款等重要信息。把它们分开放置，即使某个虚拟机出现权限配置错误或安全漏洞，也能避免影响到另一个虚拟机的消息流转。
 * 便于管理和监控：不同类型的消息有不同的管理需求。
 * 在独立的虚拟机中，运维人员可以针对普通消息和死信消息分别设置监控指标、日志记录策略等。
 * 比如，对死信消息的虚拟机设置更详细的日志记录，以便快速定位和解决消息异常问题；
 * 而对普通消息虚拟机则侧重于性能监控，确保消息的高效流转。
 * 权限控制更灵活：可以根据不同团队或角色的职责，分配不同的虚拟机访问权限。
 * 比如，开发团队负责处理普通业务逻辑，可能只需要对普通消息相关的虚拟机有读写权限；
 * 而专门处理异常情况的团队，只需要对死信消息所在的虚拟机进行操作。这样可以降低权限滥用的风险，提高系统安全性。
 * 缺点
 * 增加管理复杂度：维护多个虚拟机意味着需要管理更多的连接配置、权限设置、资源分配等。
 * 在部署和维护过程中，需要投入更多的精力确保每个虚拟机都能正常运行，增加了运维成本和出错的可能性。
 * 资源消耗增加：每个虚拟机在 RabbitMQ 服务器中都需要占用一定的系统资源，
 * 如内存、CPU 等。如果设置过多的虚拟机，可能会导致服务器资源紧张，影响整体性能。
 *
 * 放在相同虚拟机的情况
 * 优点
 * 管理简单：所有相关的交换机和队列都集中在一个虚拟机中，便于统一管理和维护。
 * 无论是配置连接、查看消息状态，还是进行故障排查，都不需要在多个虚拟机之间切换，降低了管理成本和复杂度。
 * 资源利用高效：在同一个虚拟机中，普通消息和死信消息可以共享一些底层资源，
 * 比如网络连接池、缓存等。相比于多个虚拟机，能更有效地利用服务器资源，减少资源浪费，在资源有限的情况下更具优势。
 * 方便消息流转配置：在配置死信消息的转发规则时，由于在同一虚拟机内，
 * 更容易进行交换机和队列之间的绑定操作，不需要考虑不同虚拟机之间的跨域访问等复杂问题，简化了配置流程。
 * 缺点
 * 隔离性不足：如果某个普通队列或交换机出现问题，比如遭受恶意攻击或被错误配置，
 * 可能会影响到死信相关的交换机和队列，导致死信消息无法正常处理，增加了故障扩散的风险。
 * 权限管理受限：当需要对普通消息和死信消息进行不同的权限分配时，
 * 在同一个虚拟机中实现起来相对困难，可能需要通过更复杂的权限策略来区分不同角色对不同类型消息的操作权限。
 */
public class DirectRabbitConfig {
    /**
     * 常量定义（队列、交换机、路由键）
     * 作用：定义业务队列、交换机、路由键，
     * 以及死信队列（处理消息异常 / 过期等情况的 “备用队列” ）、
     * 死信交换机、死信路由键的名称，方便统一管理、复用。
     * <p>
     * 对应架构：
     * QUEUE_NAME 对应图里 Broker 内部的 Queue；
     * EXCHANGE_NAME 对应 Exchange ；
     * ROUTING 是消息从 Exchange 路由到 Queue 的 “匹配钥匙” 。
     * 死信相关常量同理，对应专门处理死信的一套队列、交换机、路由键。
     */
    public static final String QUEUE_NAME = "DirectQueue";
    public static final String EXCHANGE_NAME = "DirectExchange";
    public static final String ROUTING = "DirectRouting";

    public static final String DLX_QUEUE_NAME = "DlxDirectQueue";
    public static final String DLX_EXCHANGE_NAME = "DlxDirectExchange";
    public static final String DLX_ROUTING = "DlxDirectRouting";

    /**
     * 队列, 起名: DirectQueue
     *
     * @return
     */
    @Bean
    public Queue directQueue() {
        // durable:(耐用)是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启
        //时仍然存在，暂存队列：当前连接有效
        // exclusive:(独家的)默认也是false，只能被当前创建的连接使⽤，⽽且当连接关闭后队列即被
        //删除。此参考优先级⾼于durable
        // autoDelete:是否⾃动删除，当没有⽣产者或者消费者使⽤此队列，该队列会⾃动删除。
        // return new Queue("DirectQueue",true,true,false);
        // ⼀般设置⼀下队列的持久化就好,其余两个就是默认false
        /*
          参数含义：
          QUEUE_NAME：队列名称，Broker 里创建同名队列。
          durable: true：队列持久化，Broker 重启后队列结构还在（消息持久化需结合生产者、交换机配置）。
          exclusive: false：非排他队列，多个连接（或信道）可访问。
          autoDelete: false：不会因 “没人用” 自动删除，保证队列稳定存在。
          对应架构：就是图里 Broker 内部 Virtual Host 下的 Queue ，
          用于存储生产者发的正常业务消息，等消费者来取。
         */
        //为 DirectQueue 配置死信交换机
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME); // 指定死信交换机
        args.put("x-dead-letter-routing-key", DLX_ROUTING);    // 指定死信路由键
        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    /**
     * Direct交换机, 起名DirectExchange
     *
     * @return
     */
    @Bean
    DirectExchange directExchange() {
        /**
         * 参数含义：
         * EXCHANGE_NAME：交换机名称，Broker 里创建同名 Direct 类型交换机。
         * durable: true：交换机持久化，Broker 重启后不丢失。
         * autoDelete: false：不会因 “没人绑队列” 自动删除。
         * 对应架构：对应图里 Broker 内部 Virtual Host 下的 Exchange ，
         * 类型是 Direct（按路由键精准匹配路由消息），接收生产者消息，再根据路由键转发。
         */
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * 绑定, 将队列和交换机绑定, 并设置用于匹配键: DirectRouting
     *
     * @return
     */
    @Bean
    Binding bindingDirect() {
        /**
         * 作用：把 directQueue 队列和 directExchange 交换机用 ROUTING 路由键绑定。
         * 这样，发往 directExchange 、路由键匹配 ROUTING 的消息，会被路由到 directQueue 。
         * 对应架构：就是图里 Exchange 到 Queue 的连线，明确消息从哪个交换机，
         * 按啥规则（路由键），发到哪个队列。
         */
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(ROUTING);
    }

    /**
     * 死信队列
     *
     * @return
     */
    @Bean
    public Queue dlxQueue() {
        /**
         * 作用：定义死信队列，参数 durable: true 保证持久化。
         * 当业务队列里的消息满足 “死信条件”（比如消费失败、消息过期等），会被转发到这个队列。
         * 对应架构：Broker 内部专门存 “死信” 的 Queue ，
         * 和业务队列逻辑隔离，用于后续处理异常消息（比如人工排查、重试等）
         */
        return new Queue(DLX_QUEUE_NAME, true);
    }

    /**
     * 死信交换机
     *
     * @return
     */
    @Bean
    DirectExchange dlxExchange() {
        /**
         * 作用：创建死信专用的 Direct 交换机，参数含义同业务交换机，持久化且不会自动删除。
         * 死信队列通过它接收 “转过来” 的死信消息。
         * 对应架构：Broker 里处理死信的 Exchange ，和业务交换机分开，避免干扰正常消息流程。
         */
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    /**
     * 绑定死信队列和交换机, 设置用于配置的键, DLX_ROUTING
     *
     * @return
     */
    @Bean
    Binding bingingDlx() {
        /**
         * 作用：把死信队列 dlxQueue 和死信交换机 dlxExchange 用 DLX_ROUTING 路由键绑定。
         * 业务队列配置死信规则后，满足条件的消息会被 “丢” 到这个交换机，再路由到死信队列。
         * 注意：如果这里方法没加 @Bean 注解，Spring 不会自动创建绑定！
         * 实际要让死信绑定生效，得补 @Bean ，否则死信转发逻辑不通。
         */
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING);
    }

    /**
     * 作用：让 RabbitMQ 支持 JSON 格式消息序列化 / 反序列化。
     * 生产者发消息时，把 Java 对象转成 JSON ；消费者接收时，再把 JSON 转回对象，方便跨系统、跨语言交互。
     * 对应流程：生产者发消息，框架用它转成 JSON 进队列；
     * 消费者取消息，用它转回业务对象，隐藏了序列化细节。
     *
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
