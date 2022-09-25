package com.practise.server.config;


import com.practise.server.service.CustomRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HzeLng
 * @version 1.0
 * @description ShiroConfig
 * @date 2020/12/26 11:40
 */
@Configuration
public class ShiroConfig {

    @Bean
    public CustomRealm customRealm(){
        return new CustomRealm();
    }

    /**
     * 配置securityManager安全管理器，主要起到一个桥梁作用.
     * @return
     */
    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(customRealm());
        return defaultWebSecurityManager;
    }

    /**
     * 进行全局配置，Filter工厂。设置对应的过滤条件和跳转条件，
     * 有自定义的过滤器，有shiro认证成功后，失败后，退出后等跳转的页面，有静态页面等内容的权限范围。
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        shiroFilterFactoryBean.setLoginUrl("/to/login");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauth");

        //设置过滤页面 认证顺序是从上往下执行
        Map<String,String> filterChainDefinitionMap = new HashMap<>();
        filterChainDefinitionMap.put("/to/login","anon");
//        filterChainDefinitionMap.put("/kill/execute/*","authc");
//        filterChainDefinitionMap.put("/item/detail/*","authc");
        filterChainDefinitionMap.put("/**","anon");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }
}
