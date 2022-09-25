package com.practise.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author HzeLng
 * @version 1.0
 * @description MailDto
 * @date 2020/12/4 15:37
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MailDto implements Serializable {

    /**
     * 分别为
     * 邮件主题
     * 邮件内容
     * 邮件接收人
     */
    private String subject;

    private String content;

    private String[] tos;
}
