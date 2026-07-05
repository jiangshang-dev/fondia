package org.jeecg.modules.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI 流式聊天专用线程池。
 * <p>
 * 不使用 {@link org.jeecg.common.util.ShiroThreadPoolExecutor}：App 端走 fd_user JWT + anon，
 * 异步任务不应依赖 Shiro / HttpServletRequest，主线程提前捕获所需参数即可。
 */
@Configuration
public class CsAiAsyncExecutorConfig {

    public static final String CS_AI_EXECUTOR = "csAiTaskExecutor";

    @Bean(CS_AI_EXECUTOR)
    public Executor csAiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(256);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("cs-ai-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
