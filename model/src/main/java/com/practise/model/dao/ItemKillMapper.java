package com.practise.model.dao;


import com.practise.model.bean.ItemKill;
import com.practise.model.bean.ItemKillExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 如果在子modules中的pom不加入 spring boot starter 就无法注解springboot相关的
 * 仅仅靠父模块的无法
 */
@Repository
public interface ItemKillMapper {

    /**
     * 获取待秒杀商品列表
     * @return
     */
    List<ItemKill> selectAll();

    /**
     * 获取指定Id的商品
     * @param id
     * @return
     */
    ItemKill selectById(@Param("id") Integer id);

    ItemKill selectByIdV2(@Param("id") Integer id);

    long countByExample(ItemKillExample example);

    int deleteByExample(ItemKillExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ItemKill record);

    int insertSelective(ItemKill record);

    List<ItemKill> selectByExample(ItemKillExample example);

    ItemKill selectByPrimaryKey(Integer id);

    /**
     * 更新待秒杀商品的库存 扣减数量操作
     * @param killId
     * @return
     */
    int updateKillItem(@Param("killId") Integer killId);

    int updateKillItemV2(@Param("killId") Integer killId);

    int updateByExampleSelective(@Param("record") ItemKill record, @Param("example") ItemKillExample example);

    int updateByExample(@Param("record") ItemKill record, @Param("example") ItemKillExample example);

    int updateByPrimaryKeySelective(ItemKill record);

    int updateByPrimaryKey(ItemKill record);
}