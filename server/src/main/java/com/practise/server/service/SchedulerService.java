package com.practise.server.service;


import com.practise.model.bean.ItemKillSuccessDiffTime;
import com.practise.model.dao.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author HzeLng
 * @version 1.0
 * @description SchedulerService
 *              这个service无需被显式调用，在MainApplication中 添加注解@EnableScheduling 即可
 * @date 2020/12/6 19:57
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private Environment env;



    /**
     * 定时获取status=0的订单并判断是否超过了TTL，然后进行失效
     *
     * 每30分钟
     */
    @Scheduled(cron = "0 0/30 * * * ? ")
    public void schedulerExpireOrders(){
        try{

            log.info("V1 scheduler");

            List<ItemKillSuccessDiffTime> itemKillSuccessDiffTimeList = itemKillSuccessMapper.selectExpiredOrder();
            if(itemKillSuccessDiffTimeList!=null && !itemKillSuccessDiffTimeList.isEmpty()){
                itemKillSuccessDiffTimeList.stream().forEachOrdered(new Consumer<ItemKillSuccessDiffTime>() {
                    @Override
                    public void accept(ItemKillSuccessDiffTime i) {
                        if(i!=null && i.getDiffTime()>=env.getProperty("scheduler.expire.orders.time",Integer.class)){
                            log.info("there exists a order whose difftime is >= 30");
                            itemKillSuccessMapper.expireOrder(i.getCode());

                        }
                    }
                });
            }

        }catch (Exception e){
            log.error("schedulerExpireOrders -error ",e.fillInStackTrace());
        }

    }
/*
    @Scheduled(cron = "0/11 * * * * ? ")
    public void schedulerExpireOrdersV2(){
        log.info("V2 scheduler");
        try {
            log.info("V2 ready sleep");
            log.info("V2 end sleep");
        }catch (Exception e){
            log.error("sleep error");
        }

    }

    @Scheduled(cron = "0/10 * * * * ? ")
    public void schedulerExpireOrdersV3(){
        log.info("V3 scheduler");
    }*/
}
