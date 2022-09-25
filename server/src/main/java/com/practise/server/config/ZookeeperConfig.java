package com.practise.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author HzeLng
 * @version 1.0
 * @description ZookeeperConfig
 * @date 2020/12/25 11:49
 */
//@Configuration
public class ZookeeperConfig {

    @Autowired
    private Environment env;

    @Bean
    public CuratorFramework curatorFramework(){
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().
                connectString(env.getProperty("zk.host"))
                .namespace(env.getProperty("zk.namespace"))
                //重试策略
                .retryPolicy(new RetryNTimes(5,1000))
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

}
