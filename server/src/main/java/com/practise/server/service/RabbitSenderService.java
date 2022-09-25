package com.practise.server.service;

import com.practise.model.dao.ItemKillSuccessMapper;
import com.practise.model.dto.KillSuccessUserInfo;
import com.practise.server.dto.KillDto;
import com.practise.server.utils.RedisUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author HzeLng
 * @version 1.0
 * @description RabbitSenderService
 *  RabbitMQ发送邮件服务
 * @date 2020/12/3 15:51
 */
@Service
public class RabbitSenderService {

    public static final Logger log= LoggerFactory.getLogger(RabbitSenderService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    public void sendKillSuccessEmailMsg(String orderNumber){
        log.info("rabbitMQ into the orderNumber  is :{}",orderNumber);

        try{

            if(StringUtil.isNotBlank(orderNumber)){
                log.info("the orderNumber is notBlank and it is ",orderNumber);
                KillSuccessUserInfo killSuccessUserInfo = itemKillSuccessMapper.selectByCode(orderNumber);
                if(killSuccessUserInfo!=null){
                    //TODO:RabbitMQ发送消息的逻辑
                    log.info("the killSuccessUserInfo is ",killSuccessUserInfo);

                    //配置消息队列模型
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.email.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.email.routing.key"));

                    /**
                     * 构建一个消息容器，存放内容为 killSuccessUserInfo
                     * Message message = MessageBuilder.withBody(orderNumber.getBytes("UTF-8")).build();
                     * 打包（转化格式）并（作为生产者）经由交换机发送到消息队列等待消费者“消费”
                     *
                     * 设置消息处理器
                     */
                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            //保证消息可靠性，设置消息的持久化
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            log.info("ready to send message to queue");
                            return message;
                        }
                    });
                }

            }



        }catch (Exception e){
            log.error("RabbitMQ mail-error",orderNumber,e.fillInStackTrace());
        }
    }

    /**
     * 秒杀成功后生成抢购订单-发送消息入死信队列，等待着一定时间失效超时未支付的订单
     *
     * @param ordeCode
     */
    public void sendKillSuccessOrderExpireMsg(final String ordeCode){
        log.info("Expire Msd func ready to start!!!");
        try{
            log.info("order code ithe orderNumber  is :{}",ordeCode);
            if(StringUtil.isNotBlank(ordeCode)){
                log.info("into the sendKillSuccessOrderExpireMsg and orderCode is not blank");
                KillSuccessUserInfo killSuccessUserInfo =  itemKillSuccessMapper.selectByCode(ordeCode);
                if(killSuccessUserInfo!=null){
                    log.info("killSuccessUserInfo!=null and ready to set the deadQueue params");
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.kill.dead.prod.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.kill.dead.prod.routing.key"));

                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            //保证消息可靠性，设置消息的持久化
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            //设置消息头部，用KillSuccessUserInfo作为接收消息实体类
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);

                            //动态设置TTL（为了测试方便，设置为10s，正常可能15分钟或者更长）
                            messageProperties.setExpiration(env.getProperty("mq.kill.item.success.kill.expire"));
                            log.info("ready to send message to dead queue");
                            return message;
                        }
                    });

                }
            }
        }catch (Exception e){
            log.error("sendKillSuccessOrderExpireMsg-dead queue error",ordeCode,e.fillInStackTrace());
        }
    }

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 发送秒杀请求，异步下订单，系统扣减库存
     * @param killId
     * @param userId
     */
    public void sendKillIdUserId(Integer killId, Integer userId){
        KillDto killDto = new KillDto();
        killDto.setKillId(killId);
        killDto.setUserId(userId);
        //配置消息队列模型
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.email.exchange"));
        rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.email.routing.key"));

        /**
         * 构建一个消息容器，存放内容为 killSuccessUserInfo
         * Message message = MessageBuilder.withBody(orderNumber.getBytes("UTF-8")).build();
         * 打包（转化格式）并（作为生产者）经由交换机发送到消息队列等待消费者“消费”
         *
         * 设置消息处理器
         */
        rabbitTemplate.convertAndSend(killDto, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                //保证消息可靠性，设置消息的持久化
                messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                return message;
            }
        });

        String key = new StringBuilder().append("buyed-").append(killId).toString();
        redisUtil.incr(key,1);
        redisUtil.incr("send",1);
    }


}
