package com.practise.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author HzeLng
 * @version 1.0
 * @description UserEntity
 * @date 2021/1/4 15:09
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    private Integer id;

    private String userName;

    private String password;

    private String phone;

    private String email;

    private Byte isActive;

    private Date createTime;

    private Date updateTime;

}
