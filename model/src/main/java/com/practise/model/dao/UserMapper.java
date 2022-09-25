package com.practise.model.dao;


import com.practise.model.bean.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(UserKey key);

    int insert(UserWithBLOBs record);

    int insertSelective(UserWithBLOBs record);

    List<UserWithBLOBs> selectByExampleWithBLOBs(UserExample example);

    List<User> selectByExample(UserExample example);

    UserWithBLOBs selectByPrimaryKey(UserKey key);

    int updateByExampleSelective(@Param("record") UserWithBLOBs record, @Param("example") UserExample example);

    int updateByExampleWithBLOBs(@Param("record") UserWithBLOBs record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(UserWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(UserWithBLOBs record);

    int updateByPrimaryKey(User record);

    UserEntity selectByUserNamePsd(@Param("userName") String userName, @Param("password") String password);

    UserEntity selectByUserName(@Param("userName") String userName);
}