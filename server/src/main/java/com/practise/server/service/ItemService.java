package com.practise.server.service;


import com.practise.model.bean.ItemKill;

import java.util.List;

/**
 * @author HzeLng
 * @version 1.0
 * @description ItemService
 * @date 2020/12/2 20:11
 */
public interface ItemService {

    /**
     * 获取秒杀信息
     * @return
     * @throws Exception
     */
    List<ItemKill> getKillItems() throws Exception;

    /**
     * 获取指定id商品的详情信息
     * @param id
     * @return
     * @throws Exception
     */
    ItemKill getKillDetail(Integer id) throws Exception;
}
