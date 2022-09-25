package com.practise.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author HzeLng
 * @version 1.0
 * @description SchedulerConfig
 * 定时任务多线程处理的通用化配置
 * @date 2020/12/6 21:02
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        /**
         * 不建议使用下面这种Executors方式创建线程池 有OOM风险
         *  scheduledTaskRegistrar.setScheduler(Executors.newScheduledThreadPool(10));
         *  而是用下面这种手动创建线程池的方法会好一点 可以人为设置参数 规避资源问题
         * 基本参数
         */

//创建一个线程池调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        //设置线程池容量
        scheduler.setPoolSize(20);
        //线程名前缀
        scheduler.setThreadNamePrefix("task-");
        //等待时常
        scheduler.setAwaitTerminationSeconds(60);
        //当调度器shutdown被调用时等待当前被调度的任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        //设置当任务被取消的同时从当前调度器移除的策略
        scheduler.setRemoveOnCancelPolicy(true);

        scheduledTaskRegistrar.setScheduler(scheduler);

        /*int corePoolSize = 10;

        int maximumPoolSizeSize = 100;

        long keepAliveTime = 3;

        ArrayBlockingQueue workQueue = new ArrayBlockingQueue(10);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSizeSize,keepAliveTime , TimeUnit.HOURS, workQueue,new ThreadFactoryBuilder().setNameFormat("XX-task-%d").build());

        scheduledTaskRegistrar.setScheduler(executor);*/
    }


}
