package com.practise.server.controller;

import jodd.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author HzeLng
 * @version 1.0
 * @description UserController
 * @date 2020/12/26 10:36
 */
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    Environment env;

    /**
     * 跳到登录页面
     * @return
     */
    @RequestMapping(value = {"/to/login","/unauth"})
    public String toLogin(){
        return "login";
    }

    /**
     * 登录认证逻辑
     * 大致流程图：
     *          1.input userName and password
     *          2.Shiro自封装成UserNamePasswordToken
     *          3.主体信息Subject（可以是具体的人，也可以是其他实体）【对象个体】
     *          4.Shiro的SecurityManager（用于管理所有的主体Subject）【交互逻辑】
     *          5.真正用于认证和授权的Realam（真正处理逻辑）【datasource Shiro从Realm获取安全数据，应该是存在DB的密码，进行验证】
     * @param userName
     * @param password
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(@RequestParam String userName, @RequestParam String password, ModelMap modelMap){
        String errorMsg="";
        try{
            //当认证不通过时，进行登录验证
            if(!SecurityUtils.getSubject().isAuthenticated()){

                String newPsd = new Md5Hash(password,env.getProperty("shiro.encrypt.password.salt")).toString();
                UsernamePasswordToken token = new UsernamePasswordToken(userName, newPsd);
                SecurityUtils.getSubject().login(token);
                //login()跳转至realm 处理 由SecurityManager交互
                //接下来会访问到UserShiroRealm.doGetAuthenticationInfo方法，在方法中使用传进来的username通过UserService查询用户信息
                //用户名验证通过后，从源码中可以看出接下来进行密码验证，在AuthenticatingRealm.getAuthenticationInfo方法中
            }

        }catch(UnknownAccountException e){
            errorMsg=e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch(DisabledAccountException e){
            errorMsg=e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (IncorrectCredentialsException e){
            errorMsg=e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (Exception e){
            errorMsg="用户登录异常，请重新登录or联系管理员";
            e.printStackTrace();
        }
        //如果没有错误信息即正确，那么就是登录成功，跳转至首页
        if(StringUtil.isBlank(errorMsg)){
            return "redirect:/index";
        }else{
            modelMap.addAttribute("errorMsg",errorMsg);
            return "login";
        }

    }

    @RequestMapping(value = "/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "login";
    }
}
