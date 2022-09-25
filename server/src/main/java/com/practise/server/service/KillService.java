package com.practise.server.service;

/**
 * @author HzeLng
 * @version 1.0
 * @description KillService
 * @date 2020/12/2 20:35
 */
public interface KillService {
    Boolean killItem(Integer killId, Integer userId) throws Exception;

    Boolean killItemV2(Integer killId, Integer userId) throws Exception;

    Boolean killItemV3(Integer killId, Integer userId) throws Exception;

    Boolean killItemV4(Integer killId, Integer userId) throws Exception;

    Boolean killItemV5(Integer killId, Integer userId) throws Exception;

    Boolean killItemV6(Integer killId) throws Exception;

    Boolean kilItem_ab_V2(Integer killId) throws Exception;

    Boolean kilItem_ab_V3(Integer killId) throws Exception;

    Boolean killItemV7(Integer killId, Integer userId) throws Exception;
}
