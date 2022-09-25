package com.practise.server.service.Impl;

import com.practise.model.bean.ItemKill;
import com.practise.model.bean.ItemKillSuccess;
import com.practise.model.dao.ItemKillMapper;
import com.practise.model.dao.ItemKillSuccessMapper;
import com.practise.server.enums.SysConstant;
import com.practise.server.service.KillService;
import com.practise.server.service.RabbitSenderService;
import com.practise.server.utils.RandomUtil;
import com.practise.server.utils.RedisUtil;
import com.practise.server.utils.SnowFlake;
//import org.apache.curator.framework.CuratorFramework;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author HzeLng
 * @version 1.0
 * @description KillServiceImpl
 *  秒杀服务
 * @date 2020/12/2 20:36
 */
@Service
public class KillServiceImpl implements KillService {

    private static final Logger log = LoggerFactory.getLogger(KillServiceImpl.class);

     private SnowFlake snowFlake=new SnowFlake(2,3);

    @Autowired
    ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;




    /**
     * 商品秒杀核心业务逻辑的处理-普通版本
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        System.out.println("VVVVV11111111111111111111111111111");
        log.info("killId and userId are"+killId+"and "+userId);
        Boolean ret = false;
        //判断当前用户是否已经抢购过该商品
        if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
            log.info("this user haven`t but this item and itemId is "+killId);
            //查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectById(killId);
            log.info("check the itemkill and it is "+itemKill);

            if(itemKill != null && itemKill.getCanKill() == 1){
                log.info("this item is cankill ==1");
                /**
                 * updateKillItem 根据数据库连接的配置?useAffectRows=true，如果无误的话已经将update的返回值设置为受影响的行数
                 * 因此受影响函数不为0，即为1，即代表扣减成功
                 */
                int res = itemKillMapper.updateKillItem(killId);
                log.info("the updateKillItem`s res is "+res);


                if(res>0){
                    log.info("res>0 buy this item ok");
                    //订单
                    commonRecordKillSuccessInfo(itemKill,userId);



                    ret = true;
                }
            }
        }else{
            System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
            throw new Exception("您已经抢购过该商品了");
        }
        return ret;
    }

    /**
     * 通用的方法-记录用户秒杀成功后生成的订单-并进行异步邮件消息的通知
     * @param kill
     * @param userId
     * @throws Exception
     */
    private void commonRecordKillSuccessInfo(ItemKill kill,Integer userId) throws Exception{{
        //抢购成功订单实例
        ItemKillSuccess itemKillSuccess = new ItemKillSuccess();
        //雪花片算法得到订单编号
        //String OrderNumber = String.valueOf(snowFlake.nextId());

        String orderNumber = String.valueOf(snowFlake.nextId());

        //itemKillSuccess.setCode(RandomUtil.generatorOrderCode());
        itemKillSuccess.setCode(orderNumber);
        itemKillSuccess.setCreateTime(DateTime.now().toDate());
        itemKillSuccess.setItemId(kill.getItemId());
        itemKillSuccess.setKillId(kill.getId());
        itemKillSuccess.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        itemKillSuccess.setUserId(userId.toString());

        //系统生成订购订单后，再次判断用户对该商品是否已经抢购过

        //仿照单例模式的双重检验锁算法
        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId) <= 0){
            int res = itemKillSuccessMapper.insertSelective(itemKillSuccess);

            if(res>0){
                //TODO:进行异步邮件消息的通知=rabbitmq+mail
                log.info("rabbitSenderService.sendKillSuccessEmailMsgthe orderNumber  is :{}",orderNumber);
                rabbitSenderService.sendKillSuccessEmailMsg(orderNumber);

                //TODO:入死信队列，用于“失效”超过指定的TTL时间时仍未支付支的订单
                log.info("sendKillSuccessOrderExpireMsg(orderNumber);!!@!#!#$!#$!#$2the orderNumber  is :{}",orderNumber);
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNumber);
                log.info("after !DAFADASDSAD");

            }
        }
    }}

    /**
     *商品秒杀核心业务逻辑的处理-mysql优化
     *                          超卖判断问题
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        System.out.println("VVVVV222222222222222222222222222222");
        Boolean ret = false;
        //判断当前用户是否已经抢购过该商品
        if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
            //查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);

            if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                /**
                 * updateKillItem 根据数据库连接的配置?useAffectRows=true，如果无误的话已经将update的返回值设置为受影响的行数
                 * 因此受影响函数不为0，即为1，即代表扣减成功
                 */
                int res = itemKillMapper.updateKillItemV2(killId);

                if(res>0){
                    //订单
                    commonRecordKillSuccessInfo(itemKill,userId);

                    ret = true;
                }
            }
        }else{
            throw new Exception("您已经抢购过该商品了");
        }
        return ret;
    }


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 商品秒杀核心业务逻辑的处理-redis分布式锁
     * redis的操作本身是基于分布式锁的
     * 也就是对于一般的key-value键值对的操作就是分布式的
     * 所以这里打算利用redis插入一个独一无二，能够体现用户唯一性的键值对
     * 也就是KillId+userId的组合，也代表一个用户只能抢购同一款商品一次
     * 所以此V3版本是为了解决同一用户多次点击购买，造成了多个相同订单的请求
     * V2版本对数据库优化的 主要是比较简单的解决了超卖（商品最终数量<0）的问题
     * 也利用redis操作的原子性，就是将if(cacheRes)包括的内容作为一个原子操作（注意原子粒度，粒度越大，影响性能）
     *
     * 这个版本的问题在于
     *              1：设置key-value锁，利用redis自身的单线程执行和唯一性 可以解决多个进程访问同一资源的问题
     *                      但是会造成死锁问题：当获取key-value锁后，该进程崩溃抛出异常，那么这个key-value锁 也没有被删除，就死锁了（其他进程无法再获取）
     *                      解决方法：可以引入expire失效时间
     *              2：上述的问题可以解决，但是仍然可能出现线程安全问题
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        System.out.println("VVVVV333333333333333333333333333");

        Boolean ret = false;
        if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){

            //TODO:借助Redis的原子操作实现分布式锁-对共享资源进行控制

            log.info("!#$@$@%@^%#%^&#^@#");
            //key的设置可以利用秒杀商品ID和用户ID 构成唯一性
            //value的设置利用订单号的生成方法（与订单号没有关系），只是为了生成而已
            ValueOperations valueOperations=stringRedisTemplate.opsForValue();
            final String key=new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value= RandomUtil.generatorOrderCode();
            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            Boolean cacheRes=valueOperations.setIfAbsent(key,value);
            //判断上述代码是否设置成功，如果成功表明已]经获得killId、userId的锁
            System.out.println("the cache res is ");
            System.out.println("the cacheRes is "+cacheRes);
            if(cacheRes){
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try{
                    log.info("#######################################");
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            ret = true;
                        }
                    }
                }catch (Exception e){
                    throw new Exception(("还没到抢购日期，或者已经抢购失败"));
                }finally {
                    //释放锁，判断当前value值是否等于key对应的value值
                    if(value.equals(valueOperations.get(key).toString())){
                        stringRedisTemplate.delete(key);
                    }
                }
            }

        }else{
            throw new Exception("redis分布式锁-您已经抢购过该商品了");
        }
        return ret;

    }



    @Autowired
    private RedissonClient redissonClient;

    /**
     * 商品秒杀核心业务逻辑的处理-redis分布式锁
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        System.out.println("VVVVV4444444444444444");
        Boolean ret = false;
        final String lockKey=new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        RLock rLock = redissonClient.getLock(lockKey);

        try{
            //rLock.lock(10,TimeUnit.SECONDS);
            Boolean cacheRes = rLock.tryLock(30,10,TimeUnit.SECONDS);
            if(cacheRes){
                if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            ret = true;
                        }
                    }
                }else{
                    throw new Exception("Redisson-您已经抢购过该商品了");
                }
            }

        }finally {
            rLock.unlock();
            //强制释放
            //rLock.forceUnlock();
        }


        return ret;
    }



//    @Autowired
//    CuratorFramework curatorFramework;
    //zookeeper路径
    private static final String pathPrefix = "/kill/zkLock/";
    /**
     * 商品秒杀核心业务逻辑的处理-基于zookeeper的分布式锁
     *
     * 原理是利用zk维护的树结构
     * 按先后顺序新建zkNode结点
     * 然后基于zk分布式锁的特性是哪个结点按照path新建的顺序最小-谁先获得
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        /*Boolean ret = false;

        CuratorFramework client;
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, pathPrefix+killId+userId+"-zkLock");
        try{
            if(interProcessMutex.acquire(10L,TimeUnit.SECONDS)){
                if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            ret = true;
                        }
                    }
                }else{
                    throw new Exception("Zookeeper-您已经抢购过该商品了");

                }
                return ret;
            }
        }catch (Exception e){
            throw new Exception("您已经抢购过该商品了");
        }finally {
            //释放锁
            if(interProcessMutex!=null){
                interProcessMutex.release();
            }
        }


        return ret;*/
        return null;
    }

    /**
     * test for apach ab
     * testURL: http://localhost:8092/kill/kill/execute/lock_ab?killId=2
     * @param killId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV6(Integer killId) throws Exception {
        log.info("apache-ab###########################");
        SnowFlake snowFlake = new SnowFlake(2, 3);
        Long longuserID = snowFlake.nextId();
        Integer userId = longuserID.intValue();
        Boolean ret = false;
        final String lockKey=new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        RLock rLock = redissonClient.getLock(lockKey);

        try{
            //rLock.lock(10,TimeUnit.SECONDS);
            Boolean cacheRes = rLock.tryLock(30,10,TimeUnit.SECONDS);
            if(cacheRes){
                if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            ret = true;
                        }
                    }
                }else{
                    throw new Exception("Redisson-apache-ab--您已经抢购过该商品了");
                }
            }

        }finally {
            rLock.unlock();
            //强制释放
            //rLock.forceUnlock();
        }


        return ret;
    }


    /**
     * test for apach ab
     * testURL: http://localhost:8092/kill/kill/execute/lock_ab?killId=2
     * @param killId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean kilItem_ab_V2(Integer killId) throws Exception {
        log.info("apache-ab###########################");
        SnowFlake snowFlake = new SnowFlake(2, 3);
        Long longuserID = snowFlake.nextId();
        Integer userId = longuserID.intValue();
        Boolean ret = false;
        if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){

            //TODO:借助Redis的原子操作实现分布式锁-对共享资源进行控制

            log.info("!#$@$@%@^%#%^&#^@#");
            //key的设置可以利用秒杀商品ID和用户ID 构成唯一性
            //value的设置利用订单号的生成方法（与订单号没有关系），只是为了生成而已
            ValueOperations valueOperations=stringRedisTemplate.opsForValue();
            final String key=new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value=RandomUtil.generatorOrderCode();
            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            Boolean cacheRes=valueOperations.setIfAbsent(key,value);
            //判断上述代码是否设置成功，如果成功表明已]经获得killId、userId的锁
            System.out.println("the cache res is ");
            System.out.println("the cacheRes is "+cacheRes);
            if(cacheRes){
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try{
                    log.info("#######################################");
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            ret = true;
                        }
                    }
                }catch (Exception e){
                    throw new Exception(("还没到抢购日期，或者已经抢购失败"));
                }finally {
                    //释放锁，判断当前value值是否等于key对应的value值
                    if(value.equals(valueOperations.get(key).toString())){
                        stringRedisTemplate.delete(key);
                    }
                }
            }

        }else{
            throw new Exception("redis分布式锁-您已经抢购过该商品了");
        }
        return ret;
    }


    @Override
    public synchronized Boolean kilItem_ab_V3(Integer killId) throws Exception {
        log.info("apache-ab###########################");
        SnowFlake snowFlake = new SnowFlake(2, 3);
        Long longuserID = snowFlake.nextId();
        Integer userId = longuserID.intValue();
        Boolean ret = false;
        //判断当前用户是否已经抢购过该商品
        if(itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0){
            //查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);

            if(itemKill != null && itemKill.getCanKill() == 1 && itemKill.getTotal()>0){
                /**
                 * updateKillItem 根据数据库连接的配置?useAffectRows=true，如果无误的话已经将update的返回值设置为受影响的行数
                 * 因此受影响函数不为0，即为1，即代表扣减成功
                 */
                int res = itemKillMapper.updateKillItemV2(killId);

                if(res>0){
                    //订单
                    commonRecordKillSuccessInfo(itemKill,userId);

                    ret = true;
                }
            }
        }else{
            throw new Exception("您已经抢购过该商品了");
        }
        return ret;
    }


    @Resource
    private RedisUtil redisUtil;
    /**
     * redis 预减库存 并且队列异步扣减库存
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV7(Integer killId, Integer userId) throws Exception {
        ValueOperations valueOperations=stringRedisTemplate.opsForValue();


        // 还没卖完，准备发消息给消息队列，让消息队列取异步减库存

        rabbitSenderService.sendKillIdUserId(killId,userId);


        return true;
    }
}
