package com.example.riskcontrol.model;

import java.util.Date;

/**
 * Created by sunpeak on 2016/8/6.
 */
public class Config {

    private String key;

    private String value;

    private String detail;

    private Date time;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
