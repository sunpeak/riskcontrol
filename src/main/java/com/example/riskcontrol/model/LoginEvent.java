package com.example.riskcontrol.model;

/**
 * Created by sunpeak on 2016/8/6.
 */
public class LoginEvent extends Event {

    public final static String MOBILE = "mobile";

    public final static String OPERATEIP = "operateIp";

    private String mobile;

    private String operateIp;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOperateIp() {
        return operateIp;
    }

    public void setOperateIp(String operateIp) {
        this.operateIp = operateIp;
    }

}
