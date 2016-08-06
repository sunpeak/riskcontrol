package com.example.riskcontrol.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by sunpeak on 2016/8/6.
 */
public abstract class Event {

    private static Logger logger = LoggerFactory.getLogger(Event.class);

    public final static String ID = "id";

    public final static String OPERATETIME = "operateTime";

    public final static String MOBILESEG = "mobileSeg";

    public final static String MOBILEIPAREA = "mobileIpArea";

    public final static String OPERATEIPAREA = "operateIpArea";


    /**
     * 场景
     */
    private String scene;

    /**
     * 事件id
     */
    private String id;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     * 事件评分
     */
    private int score;

    /****** TODO 以下扩展维度*****/
    /**
     * 手机号段
     */
    private String mobileSeg;

    /**
     * 手机归属地和运营商
     */
    private String mobileIpArea;

    /**
     * 操作ip归属地和运营商
     */
    private String operateIpArea;


    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getMobileSeg() {
        return mobileSeg;
    }

    @JSONField(deserialize = false)
    public void setMobileSeg(String mobileSeg) {
        this.mobileSeg = mobileSeg;
    }

    public String getMobileIpArea() {
        return mobileIpArea;
    }

    @JSONField(deserialize = false)
    public void setMobileIpArea(String mobileIpArea) {
        this.mobileIpArea = mobileIpArea;
    }

    public String getOperateIpArea() {
        return operateIpArea;
    }

    @JSONField(deserialize = false)
    public void setOperateIpArea(String operateIpArea) {
        this.operateIpArea = operateIpArea;
    }

    public int getScore() {
        return score;
    }

    @JSONField(deserialize = false)
    public void setScore(int score) {
        this.score = score;
    }


    /**
     * 计算事件评分
     *
     * @param count      当前值
     * @param level      阀值
     * @param levelScore 阀值评分
     * @param perScore   超过阀值以上，每个评分
     * @return 是否达到阈值
     */
    public boolean addScore(int count, int level, int levelScore, int perScore) {
        if (level <= 0 || levelScore <= 0 || perScore < 0) {
            return false;
        }
        if (count >= level) {
            this.score += levelScore + (count - level) * perScore;
            return true;
        }
        return false;
    }

}
