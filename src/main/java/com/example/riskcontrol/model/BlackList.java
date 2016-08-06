package com.example.riskcontrol.model;

import java.util.Date;


/**
 * 黑名单，白名单，可疑名单
 * Created by sunpeak on 2016/8/6.
 */
public class BlackList {

    /**
     * 维度
     */
    private EnumDimension dimension;

    /**
     * 类型
     */
    private EnumType type;

    /**
     * 值
     */
    private String value;

    /**
     * 时间
     */
    private Date time;

    /**
     * 详情
     */
    private String detail;


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public EnumDimension getDimension() {
        return dimension;
    }

    public void setDimension(EnumDimension dimension) {
        this.dimension = dimension;
    }

    public EnumType getType() {
        return type;
    }

    public void setType(EnumType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * 维度枚举
     */
    public static enum EnumDimension {
        MOBILE,
        IP,
        DEVICEID;
    }

    /**
     * 类型枚举
     */
    public static enum EnumType {
        BLACK,
        WHITE,
        TEMP;
    }

}
