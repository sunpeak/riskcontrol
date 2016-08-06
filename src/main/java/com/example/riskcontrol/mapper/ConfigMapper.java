package com.example.riskcontrol.mapper;

import com.example.riskcontrol.model.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Mapper
public interface ConfigMapper {

    @Select("select * from CONFIG")
    List<Config> queryAll();

    @Update("update CONFIG set value=#{value} WHERE `key`=#{key}")
    int update(Config config);

}
