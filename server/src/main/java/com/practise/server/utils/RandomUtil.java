package com.practise.server.utils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author HzeLng
 * @version 1.0
 * @description RandomUtil
 *      随机数生成工具
 *          1.时间戳+N位随机数
 *                  传统方式，字符串过长，无法排序
 *          2.雪花片算法
 *                  整体上按时间自增排序，并且整个分布式系统内不会产生ID碰撞，高效率
 * @date 2020/12/2 20:34
 */
public class RandomUtil {

    /**
     * 时间戳生成类
     * pattern "yyyyMMddHHmmssSS" 为时间戳格式
     * 最小单位为毫秒
     */
    private static final SimpleDateFormat dateFormatOne = new SimpleDateFormat("yyyyMMddHHmmssSS");

    /**
     * 高并发情况下 生成随机数，保证线程安全
     * 单例模式
     */
    private static final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    /**
     * 生成订单编号-方式一：
     *      通过时间戳+N位随机数流水号
     * @return
     */
    public static String generatorOrderCode(){
        //当前时间戳 + 四位流水号
        return dateFormatOne.format(DateTime.now().toDate())+generateRandomNumer(4);
    }

    /**
     * 生成N位随机数
     *      参数的final 有什么作用？
     * @param num num位随机数
     * @return
     */
    public static String generateRandomNumer(final int num){
        //建议用StringBuffer not StringBuilder 因为前者是线程安全的 带synchronized关键字的
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0;i<num;i++){
            //随机数范围0~9
            stringBuffer.append(threadLocalRandom.nextInt(9));
        }
        return stringBuffer.toString();
    }

}
