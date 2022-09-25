package com.practise.model.dao;


import com.practise.model.bean.RandomCode;
import com.practise.model.bean.RandomCodeExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RandomCodeMapper {
    long countByExample(RandomCodeExample example);

    int deleteByExample(RandomCodeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RandomCode record);

    int insertSelective(RandomCode record);

    List<RandomCode> selectByExample(RandomCodeExample example);

    RandomCode selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RandomCode record, @Param("example") RandomCodeExample example);

    int updateByExample(@Param("record") RandomCode record, @Param("example") RandomCodeExample example);

    int updateByPrimaryKeySelective(RandomCode record);

    int updateByPrimaryKey(RandomCode record);
}