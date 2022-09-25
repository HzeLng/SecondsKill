package com.practise.server.controller;


import com.practise.model.bean.ItemKill;
import com.practise.server.service.Impl.ItemServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author HzeLng
 * @version 1.0
 * @description ItemController
 * @date 2020/12/1 16:04
 */
@Controller
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    /**
     * 获取商品页面前缀
     */
    private static final String prefix = "item";

    @Autowired
    ItemServiceImpl itemServiceImpl;

    /**
     * @RequestMapping value表示多个前端入口可以映射到该List函数的
     *
     * @return
     */
    @RequestMapping(value = {"/","/index",prefix+"/list",prefix+"/index.html"},method = RequestMethod.GET)
    public String getItemList(ModelMap modelMap){
        try{
            //获取待秒杀商品
            List<ItemKill> list = itemServiceImpl.getKillItems();
            modelMap.put("list",list);
            log.info("get the killitemlist  is "+list);
        }catch (Exception e){
            log.error("获取待秒杀商品出错",e.fillInStackTrace());
            //出错跳转路径
            return "redirect:/base/error";
        }
        return "list";
    }

    /**
     * 根据前端发送的id参数
     * 跳转至对应的商品详情页面
     * @param id
     * @return
     */
    @RequestMapping(value = prefix+"/detail/{id}",method = RequestMethod.GET)
    public String intoDetail(@PathVariable Integer id, ModelMap modelMap){
        if(id == null || id < 0){
            return "redirect:/base/error";
        }
        try{
            ItemKill detail = itemServiceImpl.getKillDetail(id);
            modelMap.put("detail",detail);
        }catch (Exception e){
            log.error("get the detail of the killitem comes out a error ,id={}",id,e.fillInStackTrace());
        }

        return "info";
    }
}
