package com.practise.model.dao;


import com.practise.model.bean.ItemKillSuccess;
import com.practise.model.bean.ItemKillSuccessDiffTime;
import com.practise.model.bean.ItemKillSuccessExample;
import com.practise.model.dto.KillSuccessUserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemKillSuccessMapper {
    long countByExample(ItemKillSuccessExample example);

    int deleteByExample(ItemKillSuccessExample example);

    int deleteByPrimaryKey(String code);

    int insert(ItemKillSuccess record);

    int insertSelective(ItemKillSuccess record);

    List<ItemKillSuccess> selectByExample(ItemKillSuccessExample example);

    ItemKillSuccess selectByPrimaryKey(String code);

    int updateByExampleSelective(@Param("record") ItemKillSuccess record, @Param("example") ItemKillSuccessExample example);

    int updateByExample(@Param("record") ItemKillSuccess record, @Param("example") ItemKillSuccessExample example);

    int updateByPrimaryKeySelective(ItemKillSuccess record);

    int updateByPrimaryKey(ItemKillSuccess record);

    /**
     * 判断该用户对当前秒杀商品的已购买量
     * @param killId
     * @param userId
     * @return
     */
    int countByKillUserId(@Param("killId") Integer killId, @Param("userId") Integer userId);

    /**
     * 根据订单编号，获取订单消息(未支付)实体
     * @param code
     * @return
     */
    KillSuccessUserInfo selectByCode(String code);

    /**
     * 失效更新订单信息
     * @param code
     * @return
     */
    int expireOrder(@Param("code") String code);

    List<ItemKillSuccessDiffTime> selectExpiredOrder();
}