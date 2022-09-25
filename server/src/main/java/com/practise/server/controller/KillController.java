package com.practise.server.controller;


import com.google.common.util.concurrent.RateLimiter;
import com.practise.api.enums.StatusCode;
import com.practise.api.response.BaseResponse;
import com.practise.model.bean.ItemKill;
import com.practise.model.dao.ItemKillSuccessMapper;
import com.practise.model.dto.KillSuccessUserInfo;
import com.practise.server.dto.KillDto;
import com.practise.server.service.Impl.ItemServiceImpl;
import com.practise.server.service.Impl.KillServiceImpl;
import com.practise.server.utils.RandomUtil;
import com.practise.server.utils.RedisUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author HzeLng
 * @version 1.0
 * @description KillController
 * @date 2020/12/2 21:59
 */
@Controller
public class KillController implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    //页面前缀
    private static final String prefix = "kill";

    private  RateLimiter rateLimiter = RateLimiter.create(100);

    // private ConcurrentHashMap<Integer,Boolean> localOverMap = new ConcurrentHashMap<>();

    private volatile HashMap<Integer,Boolean> localOverMap = new HashMap<>();

    @Autowired
    private KillServiceImpl killServiceImpl;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    ItemServiceImpl itemServiceImpl;

    @Resource
    private RedisUtil redisUtil;

    /**
     *商品秒杀核心业务逻辑
     *
     * produces：它的作用是指定返回值类型，不但可以设置返回值类型还可以设定返回值的字符编码；
     * consumes： 指定处理请求的提交内容类型（Content-Type），例如application/json, text/html;
     *
     * @Valid 和 BindingResult 是一 一对应的，如果有多个@Valid，那么每个@Valid后面都需要添加BindingResult用于接收bean中的校验信息
     * BindingResult 是用来 当校验已经生效了，直接抛出异常。如果不想抛出异常，想返回校验信息给前端，这个时候就需要用到BindingResult了
     *
     * @param killDto killId(not blank) and usrId
     * @param bindingResult
     * @param httpSession 用来获取用户Id
     * @return
     */
    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto killDto, BindingResult bindingResult, HttpSession httpSession){

        if(bindingResult.hasErrors() || killDto.getKillId() <= 0){
            //killDto.killId is null or killId <= 0
            return new BaseResponse(StatusCode.InvalidParams);
        }

        Object uId = httpSession.getAttribute("uid");
        if(uId == null){
            return new BaseResponse(StatusCode.UserNotLogin);
        }
        Integer userId = (Integer)uId;

        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        try{
            Boolean res = killServiceImpl.killItem(killDto.getKillId(),userId);
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"商品已抢购完毕or不在抢购时间段内");
            }
        }catch (Exception e){
            baseResponse = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return baseResponse;

    }


    /**
     * 抢购成功跳转页面
     * @return
     */
    @RequestMapping(value = prefix+"/execute/success",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    /**
     * 抢购失败跳转页面
     * @return
     */
    @RequestMapping(value = prefix+"/execute/fail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }


    /**
     * 查看订单详情
     * @param orderNo  前端参数
     * @param modelMap  传入jsp页面使用的
     * @return
     */
    @RequestMapping(value = prefix+"/record/detail/{orderNo}",method = RequestMethod.GET)
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap){

        log.info("into the record detail jsp");
        if(StringUtil.isBlank(orderNo)){
            return "error";
        }
        KillSuccessUserInfo killSuccessUserInfo = itemKillSuccessMapper.selectByCode(orderNo);
        if(killSuccessUserInfo == null){
            return "error";
        }
        log.info("they are not null");
        modelMap.put("info",killSuccessUserInfo);

        return "killRecord";
    }


    /**
     * 秒杀核心业务逻辑-用来压力测试
     * 四道防线：（按照逻辑）
     *          1. 限流器
     *          2. 是否购买过
     *          3. redis分布式锁，限用户重复请求
     *          4. 是否卖完了
     *          5. redis预减库存
     *          6. 预减成功那就下订单，失败就返回
     * @param killDto
     * @param bindingResult
     * @param httpSession
     * @return
     */
    @RequestMapping(value = prefix+"/execute/lock",method ={RequestMethod. POST , RequestMethod. GET },consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeLock(@RequestBody @Validated KillDto killDto, BindingResult bindingResult, HttpSession httpSession){
        System.out.println(killDto.getKillId() + "-" + killDto.getUserId());
        redisUtil.incr("tongji",1);
        if(bindingResult.hasErrors() || killDto.getKillId() <= 0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);

        // 0。 限流器


        if(!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
            log.error("access Fail !!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("access Fail !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*&^&*^*&%*&^%$*&&)(*&^%*&&");
            return new BaseResponse(StatusCode.Fail.getCode(),"没抢到购买名额啊");
        }

        try{
            /*//不加分布式锁的前提
            Boolean res = killServiceImpl.killItemV2(killDto.getKillId(),killDto.getUserId());
*/
            //基于redis的分布式锁的前提
            // 判断此用户短期内是否购买过此商品 这里应该得将分布式锁，应对两个用户新请求同时进入if块 如果都false，那就重复了
            // String key = new StringBuilder().append("ordered-").append(killDto.getKillId()).append("-").append(killDto.getUserId()).toString();
            // 1. 查看用户是否已经购买过了，在redis中查询
            String key = "buyed" + killDto.getKillId() + "-" + killDto.getUserId();
            if(redisUtil.hasKey(key)){
                // 用户此前购买过
                System.out.println("buyed buyed buyed buyed buyed buyed buyed buyed buyed buyed ");
                return new BaseResponse(StatusCode.Fail.getCode(),"你买过了兄弟");
            }
            // 2. 分布式锁：如果没购买过，那就分布式锁，防止一个用户多次请求（分布式锁的一系列问题，宕机什么的）——这一步是让用户具备购买资格
            // 分布式锁的key
            key = "distribued-" + killDto.getKillId() + "-" + killDto.getUserId();
            String value = Thread.currentThread().getName();
            if(!redisUtil.setIfAbsent(key, value, 5)){
                System.out.println("分布式 分布式 分布式 分布式 分布式 分布式 分布式 分布式 ");
                return new BaseResponse(StatusCode.Fail.getCode(),"请勿重复请求");
            }
            // 3. 查看over结束标志，有了购买资格再看商品是否已经售罄
            // 3.1 按照之前看过的redis的QPS最高可到达10W+，后续随着连接数的增加而减少。
            // 3.2 如果是我们这个小项目，那也就够了，但是也可以想想优化的方案，内存访问？减少redis访问压力
            // TODO
            boolean over = localOverMap.get(killDto.getKillId());
            if(over){
                System.out.println("sold out sold out sold out sold out sold out sold out sold out sold out sold out ");
                return new BaseResponse(StatusCode.Fail.getCode(),"卖完了");
            }

            key = new StringBuffer().append("Goods").append(String.valueOf(killDto.getKillId())).toString();
            // redis预减库存
            Long stock = redisUtil.decr(key,1);
            if(stock < 0){
                localOverMap.put(killDto.getKillId(),true);
                log.info("redis 预减库存失败！！！！ ");
                return new BaseResponse(StatusCode.Fail.getCode(),"卖完了");
            }

            Boolean res = killServiceImpl.killItemV7(killDto.getKillId(),killDto.getUserId());
            if(!res){
                System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnn");
                return new BaseResponse(StatusCode.Fail.getCode(),"不加分布式锁-商品已抢购完毕or不在抢购时间段内");
            }
        }catch (Exception e){
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxx");
            baseResponse = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return baseResponse;

    }

    /**
     * 秒杀核心业务逻辑-用来压力测试
     * @param killId
     * @param bindingResult
     * @param httpSession
     * @return
     */
    @RequestMapping(value = prefix+"/execute/lock_ab",method ={RequestMethod. POST , RequestMethod. GET },consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeLock_ab(@RequestBody @Validated Integer killId, BindingResult bindingResult, HttpSession httpSession){

        if(bindingResult.hasErrors() || killId <= 0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        Integer userId = 2;

        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        try{
            /*//不加分布式锁的前提
            Boolean res = killServiceImpl.killItemV2(killDto.getKillId(),killDto.getUserId());
*/
            //基于redis的分布式锁的前提
            Boolean res = killServiceImpl.kilItem_ab_V3(killId);
            if(!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"不加分布式锁-商品已抢购完毕or不在抢购时间段内");
            }
        }catch (Exception e){
            baseResponse = new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return baseResponse;

    }
    // 15950453383

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * Controller 实现 InitializingBean 重写系统初始化函数
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //获取待秒杀商品
        List<ItemKill> list = itemServiceImpl.getKillItems();
//        ValueOperations valueOperations=stringRedisTemplate.opsForValue();
//        for(ItemKill itemKill:list){
//            int killId = itemKill.getId();
//            int total = itemKill.getTotal();
//            System.out.println("before redis" + total);
//            String key = new StringBuffer().append("MiaoShaGoods-").append(String.valueOf(killId)).toString();
//            String value = String.valueOf(total);
//            valueOperations.set(key,total);
//            int value_total = Integer.valueOf(valueOperations.get(key).toString());
//            System.out.println("after redis" + value_total);
//        }
        for(ItemKill itemKill:list){
            int killId = itemKill.getId();
            int total = itemKill.getTotal();
            System.out.println("before redis" + total);
            String key = new StringBuffer().append("Goods").append(String.valueOf(killId)).toString();
            String value = String.valueOf(total);
            redisUtil.set(key,total);
            int value_total = (int)redisUtil.get(key);
            System.out.println("after redis" + value_total);

            key = new StringBuilder().append("over-").append(killId).toString();
            redisUtil.set(key,false);
            System.out.println((boolean)redisUtil.get(key));
            key = new StringBuilder().append("buyed-").append(killId).toString();
            redisUtil.set(key,0);
            System.out.println(redisUtil.get(key));
            localOverMap.put(killId,false);
        }

        redisUtil.set("tongji",0);
        redisUtil.set("queue",0);
        redisUtil.set("miaosha",0);
        redisUtil.set("recv",0);
        redisUtil.set("send",0);
        System.out.println(System.getProperty("server.tomcat.max-threads"));
        // 电脑核数是16核
        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}
