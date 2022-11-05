package cc.xun.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

/**
 * @Author xun
 * @create 2022/11/3 20:11
 */
@Configuration
public class ThreadConfig {

    @Bean("taskExecutor")
    public Executor doSomethingExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.initialize();
        return executor;
    }

}
