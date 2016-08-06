package com.example.riskcontrol.mapper;

import com.example.riskcontrol.model.BlackList;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Mapper
public interface BlackListMapper {

    @Select("select * from BLACK_LIST")
    List<BlackList> queryAll();

    @Insert("insert into BLACK_LIST(dimension,type,value,detail) VALUES (#{dimension},#{type},#{value},#{detail})")
    int add(BlackList blackList);

    @Select("select * from BLACK_LIST where dimension=#{dimension} and value= #{value}")
    BlackList query(BlackList blackList);

}
