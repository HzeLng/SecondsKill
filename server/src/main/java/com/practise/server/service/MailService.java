package com.practise.server.service;

import com.practise.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * @author HzeLng
 * @version 1.0
 * @description MailService
 * @date 2020/12/4 15:35
 */
@Service
@EnableAsync
public class MailService {

    private static final Logger log= LoggerFactory.getLogger(MailService.class);

    /**
     * spring-boot-starter-mail内带的邮件发送组件
     */
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment env;

    /**
     * 发送邮件服务
     * @param mailDto
     */
    @Async
    public void sendSimpleEmail(final MailDto mailDto){

        try {
            log.info("ready to sendSimpleEmail");

            // 创建多用途邮件消息对象
            MimeMessage mailMessage = javaMailSender.createMimeMessage();
            // 创建邮件消息助手（参数2：设置为true，表示可以发送超链接、附件）
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");
            //封装实体
           // messageHelper.setText(mailDto.getContent(),true);
            messageHelper.setText("Text 内容 ",true);
            messageHelper.setTo(mailDto.getTos());
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setSubject("subject 主体");



            //发送实体
            javaMailSender.send(mailMessage);
            log.info("sendSimpleEmail -success");
        }catch (Exception e){
            log.error("sendSimpleEmail -error",e.fillInStackTrace());
        }

    }

    @Async
    public void sendHtmlEmail(final MailDto mailDto){

        try {
            log.info("ready to sendSimpleEmail");

            // 创建多用途邮件消息对象
            MimeMessage mailMessage = javaMailSender.createMimeMessage();
            // 创建邮件消息助手（参数2：设置为true，表示可以发送超链接、附件）
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");
            //封装实体
            // messageHelper.setText(mailDto.getContent(),true);
            messageHelper.setText("http://localhost:8092/kill/kill/record/detail/"+mailDto.getContent()+"    点击支付",true);
            messageHelper.setTo(mailDto.getTos());
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setSubject("subject 主体");



            //发送实体
            javaMailSender.send(mailMessage);
            log.info("sendSimpleEmail -success");
        }catch (Exception e){
            log.error("sendSimpleEmail -error",e.fillInStackTrace());
        }

    }

}
