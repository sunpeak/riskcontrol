package com.example.riskcontrol.service;

import com.alibaba.fastjson.JSON;
import com.example.riskcontrol.dao.MongoDao;
import com.example.riskcontrol.dao.RedisDao;
import com.example.riskcontrol.model.EnumTimePeriod;
import com.example.riskcontrol.model.Event;
import com.example.riskcontrol.util.DocumentDecoder;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Service
public class DimensionService {

    private static Logger logger = LoggerFactory.getLogger(DimensionService.class);

    @Autowired
    private MongoDao mongoDao;

    @Autowired
    private RedisDao redisDao;

    private String riskEventCollection = "riskevent";


    /**
     * 过期SortedSet的数据
     * 过期SortedSet键
     * 添加SortedSet值
     * 计算SortedSet时间段内的频数
     */
    private final static String luaScript = "if tonumber(ARGV[1])>0 then " +
            "redis.call('ZREMRANGEBYSCORE',KEYS[1],0,ARGV[1]);" +
            "redis.call('EXPIRE',KEYS[1],ARGV[2]);" +
            "end;" +
            "redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4]);" +
            "return redis.call('ZCOUNT',KEYS[1],ARGV[5],ARGV[6]);";

    private String luaSha;


    /**
     * lua 添加行为数据并获取结果
     */
    private Long runSha(String key, String remMaxScore, String expire, String score, String value, String queryMinScore, String queryMaxScore) {
        if (luaSha == null) {
            luaSha = redisDao.scriptLoad(luaScript);
        }
        return redisDao.evalsha(luaSha, 1, new String[]{key, remMaxScore, expire, score, value, queryMinScore, queryMaxScore});
    }


    /**
     * @param event          事件
     * @param condDimensions 条件维度数组,注意顺序
     * @param enumTimePeriod 查询时间段
     * @param aggrDimension  聚合维度
     * @return
     */
    public int addQueryHabit(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return 0;
        }

        Date operate = event.getOperateTime();
        String key1 = String.join(".", String.join(".", condDimensions), aggrDimension);
        String[] key2 = new String[condDimensions.length];
        for (int i = 0; i < condDimensions.length; i++) {
            Object value = getProperty(event, condDimensions[i]);
            if (value == null || "".equals(value)) {
                return 0;
            }
            key2[i] = value.toString();
        }
        String key = event.getScene() + "_sset_" + key1 + "_" + String.join(".", key2);

        Object value = getProperty(event, aggrDimension);
        if (value == null || "".equals(value)) {
            return 0;
        }

        int expire = 0;
        String remMaxScore = "0";
        if (!enumTimePeriod.equals(EnumTimePeriod.ALL)) {
            //如果需要过期，则保留7天数据,满足时间段计算
            expire = 7 * 24 * 3600;
            remMaxScore = dateScore(new Date(operate.getTime() - expire * 1000L));
        }

        Long ret = runSha(key, remMaxScore, String.valueOf(expire), dateScore(operate), value.toString(), dateScore(enumTimePeriod.getMinTime(operate)), dateScore(enumTimePeriod.getMaxTime(operate)));
        return ret == null ? 0 : ret.intValue();
    }


    /**
     * 计算sortedset的score
     *
     * @param date
     * @return
     */
    private String dateScore(Date date) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }


    private Object getProperty(Event event, String field) {
        try {
            return PropertyUtils.getProperty(event, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 事件入库
     *
     * @param event
     */
    public void insertEvent(Event event) {
        mongoDao.insert(event.getScene(), Document.parse(JSON.toJSONString(event), new DocumentDecoder()));
    }

    /**
     * 可疑事件入库
     *
     * @param event 事件bean
     * @param rule  触发的规则详情
     */
    public void insertRiskEvent(Event event, String rule) {
        Document document = Document.parse(JSON.toJSONString(event), new DocumentDecoder());
        document.append("rule", rule);
        mongoDao.insert(riskEventCollection, document);
        logger.warn("可疑事件，event={},rule={}", JSON.toJSONString(event), rule);
    }

    public int count(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null) {
            logger.error("参数错误");
            return 0;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return 0;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));

        return mongoDao.count(event.getScene(), query);
    }

    /**
     * db.applogin.aggregate(
     * [
     * {$match:{mobile:"13900009725", operateTime: { $gte: new Date(1467213873277) }}},
     * {$group:{_id:null,_array:{$addToSet: "$operateIp"}}},
     * {$project:{_num:{$size:"$_array"}}}
     * ]
     * )
     **/
    private int distinctCountWithMongo(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return 0;
        }

        Document query = new Document();
        for (String weido : condDimensions) {
            Object value = getProperty(event, weido);
            if (value == null || "".equals(value)) {
                return 0;
            }
            query.put(weido, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));

        return mongoDao.distinctCount(event.getScene(), query, aggrDimension);
    }

    private int distinctCountWithRedis(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        return addQueryHabit(event, condDimensions, enumTimePeriod, aggrDimension);
    }

    /**
     * 计算频数，有2种方式，这里考虑性能，采用redis方式
     *
     * @param event
     * @param condDimensions
     * @param enumTimePeriod
     * @param aggrDimension
     * @return
     */
    public int distinctCount(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        return distinctCountWithRedis(event, condDimensions, enumTimePeriod, aggrDimension);
    }

    public List distinct(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.distinct(event.getScene(), query, aggrDimension);
    }

    public Object max(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.max(event.getScene(), query, aggrDimension);
    }

    public Object min(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.min(event.getScene(), query, aggrDimension);
    }

    public Double sum(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.sum(event.getScene(), query, aggrDimension);
    }

    public Double avg(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }
        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.avg(event.getScene(), query, aggrDimension);
    }

    public Object first(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.first(event.getScene(), query, aggrDimension, new Document(Event.OPERATETIME, 1));
    }

    public Object last(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String lastDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || lastDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }
        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.last(event.getScene(), query, lastDimension, new Document(Event.OPERATETIME, 1));
    }

    public Double stdDevPop(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }

        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.stdDevPop(event.getScene(), query, aggrDimension);
    }

    public Double stdDevSamp(Event event, String[] condDimensions, EnumTimePeriod enumTimePeriod, String aggrDimension, int sampleSize) {
        if (event == null || ArrayUtils.isEmpty(condDimensions) || enumTimePeriod == null || aggrDimension == null) {
            logger.error("参数错误");
            return null;
        }
        Document query = new Document();
        for (String dimension : condDimensions) {
            Object value = getProperty(event, dimension);
            if (value == null || "".equals(value)) {
                return null;
            }
            query.put(dimension, value);
        }

        query.put(Event.OPERATETIME, new Document("$gte", enumTimePeriod.getMinTime(event.getOperateTime())).append("$lte", enumTimePeriod.getMaxTime(event.getOperateTime())));
        return mongoDao.stdDevSamp(event.getScene(), query, aggrDimension, sampleSize);
    }


}
