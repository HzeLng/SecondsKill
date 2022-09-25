package com.practise.model.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author HzeLng
 * @version 1.0
 * @description ItemKillSuccessDiffTime
 * @date 2020/12/6 20:13
 */
@Getter
@Setter
@ToString
public class ItemKillSuccessDiffTime {
    private String code;

    private Integer itemId;

    private Integer killId;

    private String userId;

    private Byte status;

    private Date createTime;

    private Integer diffTime;
}
