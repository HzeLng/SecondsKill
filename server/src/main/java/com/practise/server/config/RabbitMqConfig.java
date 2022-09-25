package com.practise.server.config;


import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;


/**
 * @author HzeLng
 * @version 1.0
 * @description RabbitMqConfig
 *
 * Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 * Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 * Queue:消息的载体,每个消息都会被投到一个或多个队列。
 * Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 * Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 * vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 * Producer:消息生产者,就是投递消息的程序.
 * Consumer:消息消费者,就是接受消息的程序.
 * Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 *
 * @date 2020/12/3 14:46
 */
@Configuration
public class RabbitMqConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /*@Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;*/

    public static final String EXCHANGE_A = "my-mq-exchange_A";
    public static final String EXCHANGE_B = "my-mq-exchange_B";
    public static final String EXCHANGE_C = "my-mq-exchange_C";


    public static final String QUEUE_A = "QUEUE_A";
    public static final String QUEUE_B = "QUEUE_B";
    public static final String QUEUE_C = "QUEUE_C";

    public static final String ROUTINGKEY_A = "spring-boot-routingKey_A";
    public static final String ROUTINGKEY_B = "spring-boot-routingKey_B";
    public static final String ROUTINGKEY_C = "spring-boot-routingKey_C";

    /**
     *这个应该是 properties 里面 mq.env=test 那里的
     */
    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        //容器工厂
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //连接
        factory.setConnectionFactory(connectionFactory);
        //消息传输格式
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //针对单一消费者设置的参数
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        //每次拉取的消息条数
        factory.setPrefetchCount(1);
        // factory.setTxSize(1);
        return factory;
    }

    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //确认消费模式-NONE
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.concurrency",int.class));
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.max-concurrency",int.class));
        /**
         * 设置prefetchCount来限制Queue每次发送给每个消费者的消息数，
            * 比如我们设置prefetchCount=1，则Queue每次给每个消费者发送一条消息；
            * 消费者处理完这条消息后Queue会再给该消费者发送一条消息。
            */
            factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.simple.prefetch",int.class));
        return factory;
}

    /**
     * 发送消息的核心组件 RabbitTemplate
     * @return
     */
    @Bean
    public RabbitTemplate rabbitTemplate(){
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        /**
         * 当mandatory标志位设置为true时
         * 如果exchange根据自身类型和消息routingKey无法找到一个合适的queue存储消息
         * 那么broker会调用basic.return方法将消息返还给生产者
         * 当mandatory设置为false时，出现上述情况broker会直接将消息丢弃
         */
        rabbitTemplate.setMandatory(true);

        /**
         * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调
         *
         * 如果消息到达exchange，则confirm回调，ack为true
         * 如果消息没有到达exchange，则confirm也回调，ack为false
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }
        });

        /**
         * ReturnCallback接口用于实现消息发送到RabbitMQ交换器，但无相应队列与交换器绑定时的回调
         */
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                log.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
//            }
//        });

        /**
         * ReturnsCallback接口用于实现消息发送到RabbitMQ交换器，但无相应队列与交换器绑定时的回调
         * 如果exchange到queue成功，则不回调
         * 如果exchange到queue不成功，则回调此函数
         */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",
                        returnedMessage.getExchange(),returnedMessage.getRoutingKey(),
                        returnedMessage.getReplyCode(),returnedMessage.getReplyText(),
                        returnedMessage.getMessage());
            }
        });
        return rabbitTemplate;
    }

    /**
     * 构建异步发送邮箱通知的消息队列Queue模型
     *  Queue is of import org.springframework.amqp.core.*;
     *
     *  RabbitMQ中的消息都只能存储在Queue中
     * 多个消费者可以订阅同一个Queue，这时Queue中的消息会被平均分摊给多个消费者进行处理
     * 而不是每个消费者都收到所有的消息并处理。
     *
     * @return
     */
    @Bean
    public Queue successEmailQueue(){
        //durable param 持久化：希望即使在RabbitMQ服务重启的情况下，也不会丢失消息——true
        return new Queue(env.getProperty("mq.kill.item.success.email.queue"),true);
    }



    /**
     * 交换机Exchange
     *生产者将消息发送到Exchange（交换器），由Exchange将消息路由到一个或多个Queue中（或者丢弃）
     *
     * ExchangeType:
     *      1.fanout:这种类型的路由器它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中。
     *      2.direct:                它会把消息路由到那些binding key与routing key完全匹配的Queue中。
     *      3.topic:与direct一样路由到binding key与routing key完全匹配的Queue中,但是更灵活，设置了模糊匹配，不那么“固执”
     *      4.headers:不依赖于routing key与binding key的匹配规则来路由消息，而是根据发送的消息内容中的headers属性进行匹配。
     *                  在绑定Queue与Exchange时指定一组键值对；当消息发送到Exchange时，RabbitMQ会取到该消息的headers（也是一个键值对的形式），
     *                  对比其中的键值对是否完全匹配Queue与Exchange绑定时指定的键值对；如果完全匹配则消息会路由到该Queue，否则不会路由到该Queue
     * @return
     */
    @Bean
    public TopicExchange successEmailExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.email.exchange"),true,false);
    }

    /**
     *
     * RabbitMQ中通过Binding将Exchange结合路由规则routingKey与Queue关联起来，这样RabbitMQ就知道如何正确地将消息路由到指定的Queue了。
     * @return
     */
    @Bean
    public Binding successEmailBinding(){
        /**
         * 生产者在将消息发送给Exchange的时候，一般会指定一个routing key，来指定这个消息的路由规则，
         * 而这个routing key需要与Exchange Type及binding key联合使用才能最终生效。
         * 生产者就可以在发送消息给Exchange时，通过指定routing key来决定消息流向哪里。
         * RabbitMQ为routing key设定的长度限制为255 bytes。
         *
         * 什么是bindingKey：
         *      在绑定（Binding）Exchange与Queue的同时，一般会指定一个binding key；
         *      消费者将消息发送给Exchange时，一般会指定一个routing key；
         *      当binding key与routing key相匹配时，消息将会被路由到对应的Queue中。
         */
        return BindingBuilder.bind(successEmailQueue()).to(successEmailExchange()).with(env.getProperty("mq.kill.item.success.email.routing.key"));
    }


    /**
     * 接下来创建的组件是死信队列，死信队列用于 秒杀成功但超时未支付的订单队列处理
     * 死信队列由 死信交换机、死信路由以及TTL存活时间组成
     */

    /**
     * 构建秒杀成功之后-订单超时未支付的死信队列消息模型-Queue
     *
     * argsMaps.put("argsValue","argsName");
     *
     * 设置TTL超时失效时间
     * 不过这次不在这里设置，在其他地方设置
     * argsMaps.put("x-message-ttl",10000);
     * @return
     */
    @Bean
    public Queue successKillDeadQueue(){

        Map<String,Object> argsMaps = Maps.newHashMap();
        argsMaps.put("x-dead-letter-exchange", env.getProperty("mq.kill.item.success.kill.dead.exchange"));
        argsMaps.put("x-dead-letter-routing-key", env.getProperty("mq.kill.item.success.kill.dead.routing.key"));

        return new Queue(env.getProperty("mq.kill.item.success.kill.dead.queue"),true,false,false,argsMaps);
    }


    /**
     *基本交换机
     * @return
     */
    @Bean
    public TopicExchange successKillDeadProdExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.kill.dead.prod.exchange"),true,false);
    }


    /**
     * 创建基本交换机+基本路由 -> 死信队列的绑定
     *
     * bind Queue to Exchange with RoutingKey
     * @return
     */
    @Bean
    public Binding successKillDeadProdBinding(){
        return BindingBuilder.bind(successKillDeadQueue())
                .to(successKillDeadProdExchange())
                .with(env.getProperty("mq.kill.item.success.kill.dead.prod.routing.key"));
    }


    /**
     * 真正的队列
     * @return
     */
    @Bean
    public Queue successKillRealQueue(){
        return new Queue(env.getProperty("mq.kill.item.success.kill.dead.real.queue"),true);
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    public TopicExchange successKillDeadExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.kill.dead.exchange"),true,false);
    }

    /**
     * 死信交换机+死信路由 -> 真正队列的绑定
     * @return
     */
    @Bean
    public Binding successKillDeadBinding(){
        return BindingBuilder.bind(successKillRealQueue())
                .to(successKillDeadExchange())
                .with(env.getProperty("mq.kill.item.success.kill.dead.routing.key"));
    }


}
