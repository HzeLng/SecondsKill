package com.practise.server.service.Impl;

import com.practise.model.bean.ItemKill;
import com.practise.model.dao.ItemKillMapper;
import com.practise.server.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HzeLng
 * @version 1.0
 * @description ItemServiceImpl
 * @date 2020/12/2 20:11
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ItemKillMapper itemKillMapper;

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    /**
     * description:
     *      获取待秒杀商品列表
     * @return
     * @throws Exception
     */
    @Override
    public List<ItemKill> getKillItems() throws Exception {
        List<ItemKill> itemKillList=itemKillMapper.selectAll();
        ItemKill itemKill = itemKillMapper.selectByPrimaryKey(1);
        System.out.println("itemkill is "+ itemKill);
        return itemKillList;

    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill itemKill = itemKillMapper.selectById(id);
        if(itemKill == null ){
            throw new Exception("指定Id的待秒杀商品不存在");
        }
        return itemKill;
    }
}