package cc.xun.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author xun
 * @create 2022/11/3 20:11
 */
@Configuration
public class ThreadConfig {

    @Bean("taskExecutor")
    public Executor doSomethingExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        // 核心线程数：线程池创建时候初始化的线程数
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        // 缓冲队列：用来缓冲执行任务的队列
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        executor.initialize();
        return executor;
    }

}
