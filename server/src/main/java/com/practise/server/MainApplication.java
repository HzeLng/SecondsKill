package com.practise.server;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author HzeLng
 * @version 1.0
 * @description MainApplication
 * @date 2020/12/1 12:04
 */
@SpringBootApplication(scanBasePackages = "com.practise.**")
@ImportResource(locations = {"classpath:spring/spring-jdbc.xml","classpath:applicationContext.xml"})
//@ImportResource("classpath:spring/spring-jdbc.xml")
//@MapperScan(basePackages = "com.HzeLng.kill.model.dao")
@MapperScan(basePackages = "com.practise.**.dao")
//允许任务调度
@EnableScheduling
/**
 * @ComponentScan 到底是个啥？ 加了这个jsp页面就不行了  @
 * @ComponentScan 会覆盖SpringBootApplication的扫描
 */
//@ComponentScan(basePackages = {"com.HzeLng.kill.server","com.HzeLng.kill.model.dao"})
public class MainApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class,args);
    }
}
