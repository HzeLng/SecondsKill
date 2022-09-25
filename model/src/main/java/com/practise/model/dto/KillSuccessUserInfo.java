package com.practise.model.dto;


import com.practise.model.bean.ItemKillSuccess;
import lombok.Data;

import java.io.Serializable;

/**
 * @author HzeLng
 * @version 1.0
 * @description KillSuccessUserInfo
 *      继承了ItemKillSuccess 因此补充了一些字段
 * @date 2020/12/3 20:40
 */
@Data
public class KillSuccessUserInfo extends ItemKillSuccess implements Serializable {

    private String userName;

    private String phone;

    private String email;

    private String itemName;

    @Override
    public String toString() {
        return "KillSuccessUserInfo{" +
                "userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
