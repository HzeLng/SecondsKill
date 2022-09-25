package com.practise.server.service;


import com.practise.model.bean.ItemKill;
import com.practise.model.bean.ItemKillSuccess;
import com.practise.model.dao.ItemKillMapper;
import com.practise.model.dao.ItemKillSuccessMapper;
import com.practise.model.dto.KillSuccessUserInfo;
import com.practise.server.dto.KillDto;
import com.practise.server.dto.MailDto;
import com.practise.server.enums.SysConstant;
import com.practise.server.utils.RedisUtil;
import com.practise.server.utils.SnowFlake;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * @author HzeLng
 * @version 1.0
 * @description RabbitReceiverService
 * @date 2020/12/3 15:53
 */
@Service
public class RabbitReceiverService {

    public static final Logger log = LoggerFactory.getLogger(RabbitSenderService.class);

    @Autowired
    MailService mailService;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;



    /**
     * 秒杀异步邮件通知-接收消息
     * queues 设置要消费的队列s，（应该）可有多个
     * containFactory 设置单一消费者模式or多消费者模式 与RabbitMqConfig里的Bean对应
     *
     * @param killSuccessUserInfo 从消息队列取出对象实体 killSuccessUserInfo
     *
     *                            如果是containerFactory = "multiListenerContainer"
     *                            应该怎么写？
     */
   // @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"},containerFactory = "multiListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo killSuccessUserInfo){

        try{
            log.info("take the message from queues",killSuccessUserInfo);
            //TODO:Server take message from queues and send this message as a consumer
            MailDto mailDto = new MailDto(env.getProperty("mail.kill.item.success.subject"),
                                          killSuccessUserInfo.getCode(),
                                          new String[]{killSuccessUserInfo.getEmail()});

            //mailService.sendSimpleEmail(mailDto);
            // 应该是要发送邮件的，但是为了压测选择不发送邮件了
            //mailService.sendHtmlEmail(mailDto);
            log.info("actually send the message");
        }catch (Exception e){
            log.error("kill mail message - receiveMessage-error",e.fillInStackTrace());
        }

    }

    /**
     * 用户秒杀成功后超时未支付-监听者
     *
     * @param killSuccessUserInfo
     */
    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "multiListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo killSuccessUserInfo){
        try{
            if(killSuccessUserInfo!=null){
                log.info("Time! Check the Order Status == 0 or not ");
                //primary key will be faster;
                ItemKillSuccess itemKillSuccess = itemKillSuccessMapper.selectByPrimaryKey(killSuccessUserInfo.getCode());
                //判断订单状态是否为0，即失效时间过了还是未支付状态，那么可以修改为无效订单
                if(itemKillSuccess!=null &&itemKillSuccess.getStatus().intValue() == 0){
                    itemKillSuccessMapper.expireOrder(itemKillSuccess.getCode());
                    log.info("Time! Order Expired");


                    //TODO:订单失效后应该恢复库存
                }
            }

            log.info("consumeExpireOrder-done");
        }catch (Exception e){
            log.error("consumeExpireOrder-error",e.fillInStackTrace());
        }

    }


    @Autowired
    private RedisUtil redisUtil;

    /**
     * 消费 秒杀请求
     * 真正下订单之前，再检查：
     * 1. redis是否有订单记录了
     * 2. 数据库查询是否还有库存（因为这里是要对数据库进行减库存了）
     * 3.
     * @param killDto
     */
    @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"},containerFactory = "multiListenerContainer")
    public void consumeKillRequest(KillDto killDto){ //KillDto killDto, Channel channel
        log.info("当前线程是{}！！！",Thread.currentThread().getName());
        redisUtil.incr("recv",1);
        try{
            // 不应该数据库查询 而是redis再次查看是否买过了
//            if(itemKillSuccessMapper.countByKillUserId(killDto.getKillId(), killDto.getUserId()) > 0){
//                // 数据库 已购买订单，显示已经购买过了
//                log.warn("兄弟，数据库显示你已经买过了！！！！！！！！");
//                return ;
//            }
            String orderNum = (String)redisUtil.get(killDto.getKillId() + "-" + killDto.getUserId());
            if(orderNum != null){
                log.warn("兄弟，redis显示你已经买过了！！！！！！！！");
                return ;
            }
            // 再次查看是否还有库存
            ItemKill itemKill = itemKillMapper.selectById(killDto.getKillId());
            if(itemKill == null){
                log.error("未找到此秒杀商品！！！！！！！！！！！！！！");
                return ;
            }
            if(itemKill.getTotal() < 0){
                log.warn("数据库显示卖完了！！！！！！！！！！！！！");
            }
            // 到这里说明可以正式买了

            miaosha(killDto.getKillId(), killDto.getUserId());


        }catch (Exception e){
            log.error("kill mail message - receiveMessage-error",e.fillInStackTrace());
        }

    }
    private SnowFlake snowFlake=new SnowFlake(2,3);

    /**
     * 1. 减库存
     * 2. 数据库加订单
     * 3. redis加订单
     * @param killId
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    public void miaosha(Integer killId, Integer userId){
        redisUtil.incr("miaosha",1);
        // 先减库存
        int res = itemKillMapper.updateKillItemV2(killId);
        if(res < 0){
            log.error("数据库扣减失败，已经小于0");
            // redis设置秒杀结束标记
            redisUtil.set("over-" + killId, true);
            return ;
        }
        //抢购成功订单实例
        ItemKillSuccess itemKillSuccess = new ItemKillSuccess();
        //雪花片算法得到订单编号
        //String OrderNumber = String.valueOf(snowFlake.nextId());

        String orderNumber = String.valueOf(snowFlake.nextId());

        //itemKillSuccess.setCode(RandomUtil.generatorOrderCode());
        itemKillSuccess.setCode(orderNumber);
        itemKillSuccess.setCreateTime(DateTime.now().toDate());
        itemKillSuccess.setItemId(8);
        itemKillSuccess.setKillId(killId);
        itemKillSuccess.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        itemKillSuccess.setUserId(userId.toString());

        int res1 = itemKillSuccessMapper.insertSelective(itemKillSuccess);
        redisUtil.set("buyed" + killId + "-" + userId,orderNumber);
        if(res1 < 0){
            log.error("数据库写入秒杀订单失败");
        }

        // 在这里解锁 分布式锁
        // key = "distribued-" + killDto.getKillId() + "-" + killDto.getUserId();

    }
}
