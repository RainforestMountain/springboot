package com.example.lotterysystem.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * @Configuration：标记该类是 Spring 的配置类，Spring 启动时会加载其中的 Bean 定义（如 asyncServiceExecutor 线程池 Bean ）。
 * @EnableAsync：开启 Spring 的异步方法支持，配合 @Async 注解，让标注的方法在独立线程中执行。
 * @Data：Lombok 注解，自动生成 getter、setter、toString 等方法，简化代码。
 */
@EnableAsync
@Configuration
@Data
public class ExecutorConfig {
    /**
     * 通过 @Value 从配置文件（如 application.yml/application.properties ）
     * 读取线程池参数：
     * <p>
     * corePoolSize：线程池核心线程数（即使线程空闲，也会保留的线程数量 ）。
     * maxPoolSize：线程池最大线程数（线程池能创建的最大线程数量，应对突发高并发 ）。
     * queueCapacity：任务队列容量（核心线程忙时，新任务会先进入队列等待 ）。
     * namePrefix：线程名称前缀，方便日志排查线程归属。
     */
    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix;

    /**
     * 创建并配置 ThreadPoolTaskExecutor（Spring 对 ThreadPoolExecutor 的封装，适配 Spring 异步任务 ），步骤：
     * <p>
     * 创建线程池对象：new ThreadPoolTaskExecutor() 实例化线程池。
     * 设置核心参数：
     * setCorePoolSize(corePoolSize)：核心线程数，决定线程池基础 “常驻” 线程数量。
     * setMaxPoolSize(maxPoolSize)：最大线程数，业务高峰时可扩容的线程上限。
     * setQueueCapacity(queueCapacity)：任务队列容量，存放不下的任务会触发线程扩容（直到 maxPoolSize ）。
     * setKeepAliveSeconds(3)：空闲线程存活时间（非核心线程空闲超过此时间会被销毁，释放资源 ）。
     * setThreadNamePrefix(namePrefix)：线程命名前缀，生成的线程名如 namePrefix-1、namePrefix-2 。
     * 设置拒绝策略：
     * setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
     * AbortPolicy:表示当线程池达最大线程数、队列也满时，直接拒绝新任务，抛出 RejectedExecutionException 异常。
     * 常用拒绝策略还有：
     * CallerRunsPolicy：调用者线程（提交任务的线程 ）执行任务，缓解压力。
     * DiscardOldestPolicy：丢弃队列中最老的任务，尝试提交新任务。
     * DiscardPolicy：直接丢弃新任务，不抛异常。
     * 初始化线程池：initialize() 完成线程池初始化，让线程池 ready 接收任务。
     *
     * @return
     */
    @Bean(name = "asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new
                ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(3);
        threadPoolTaskExecutor.setThreadNamePrefix(namePrefix);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执⾏任务，⽽是由调⽤者所在的线程来执⾏
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //加载
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    //1. 线程池工作流程（基于配置参数 ）
    //当用 @Async("asyncServiceExecutor") 标注方法，Spring 会把方法调用包装成任务，提交给该线程池，执行逻辑：
    //
    //任务提交：调用异步方法 → 任务进入线程池。
    //核心线程处理：若当前运行线程数 < corePoolSize → 直接创建新线程执行任务。
    //队列缓冲：若核心线程满 → 任务进入 queueCapacity 容量的队列等待。线程数 ≥ corePoolSize
    //扩容处理：队列满且当前线程数 < maxPoolSize → 创建新线程（非核心线程 ）执行任务。//不满不执行, 懒汉
    //拒绝策略：队列满且线程数达 maxPoolSize → 触发 AbortPolicy，抛异常拒绝任务。
    //2. 线程池参数设计逻辑
    //corePoolSize：根据业务 “基准并发量” 设置，比如系统平时稳定有 10 个异步任务同时执行，可设为 10 。
    //maxPoolSize：应对突发流量，如秒杀活动时异步任务激增，设为 50 （需结合服务器 CPU、内存资源 ）。
    //queueCapacity：平衡内存占用和任务缓冲，过大可能内存溢出，过小易触发扩容 / 拒绝，需压测调整。
    //keepAliveSeconds：非核心线程空闲超时时间，设为 3 秒，让空闲线程快速释放，节省资源。
}
