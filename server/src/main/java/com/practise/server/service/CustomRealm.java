package com.practise.server.service;


import com.practise.model.bean.UserEntity;
import com.practise.model.dao.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author HzeLng
 * @version 1.0
 * @description CustomRealm
 *              用户自定义的realm-用于shiro的认证、授权
 * @date 2020/12/26 11:48
 */
public class CustomRealm extends AuthorizingRealm {

    private static final Logger log = LoggerFactory.getLogger(CustomRealm.class);

    private static final Long SESSION_TIME_OUT = 30_000L;

    @Autowired
    private UserMapper userMapper;


    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }


    /**
     * 认证-登录
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        //get the userName and password from 【the input of user】
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String userName = token.getUsername();
        String password =  String.valueOf(token.getPassword());
        log.info("the current userName is {}, and the password is {}",userName,password);

        /** a check from user`s DB to check if this userName exists firstly ?
        UserVO user = userService.selectUserByUserName(userName);
        if (null == user) {
            throw new BugException("未知账号");
        }*/

        UserEntity user = userMapper.selectByUserName(userName);
        if(user == null){
            throw new UnknownAccountException("this userAccount dosen`t exists");
        }
        //判断用户当前的账户可用性 throw new
        if(!Objects.equals(1,user.getIsActive().intValue())){
            throw new DisabledAccountException("this userAccount i disabled");
        }
        //判断用户当前密码是否匹配数据库
        if(!user.getPassword().equals(password)){
            throw new IncorrectCredentialsException("the password doesn`t match the db`s password");
        }
        //将查询出来的用户名及密码，封装到SimpleAuthenticationInfo 对象中，并返回，(用于接下来的密码验证 在这里已经验证过了)
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user.getUserName(),password,getName());
        //登录成功后将一些信息存到Session中
            //当shiro和SpringMVC等web框架整合在一起时，session会交给HTTPsession管理
        setSession("uid",user.getId());
        return  simpleAuthenticationInfo;
    }

    /**
     * shiro具有session管理功能
     *
     * 将key与对应的value塞入shiro的session中-最终交给httpSession管理（如果是分布式Session，那么就是交给redis管理）
     * @param key
     * @param value
     */
    private void setSession(String key,Object value){
        Session session = SecurityUtils.getSubject().getSession();
        if(session!=null){
            session.setAttribute(key,value);
            //30s
            session.setTimeout(SESSION_TIME_OUT);
        }
    }
}
