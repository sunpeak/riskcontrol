package com.example.riskcontrol.model;


/**
 * rest响应结果
 * Created by sunpeak on 2016/8/6.
 */
public class Result<T> {

    /**
     * 结果标志:true 成功 false 失败
     */
    private boolean success;

    /**
     * 结果返回码
     */
    private String retCode;

    /**
     * 结果返回消息
     */
    private String retMsg;

    /**
     * 结果返回数据
     */
    private T data;

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
        this.setRetMsg(CodeMap.getMsgbyCode(retCode));
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public static <T> Result<T> success() {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setRetCode(CodeMap.OK);
        return r;
    }

    public static <T> Result<T> fail() {
        Result<T> r = new Result<T>();
        r.setSuccess(false);
        r.setRetCode(CodeMap.FAIL);
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}

