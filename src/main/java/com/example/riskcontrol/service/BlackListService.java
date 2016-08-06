package com.example.riskcontrol.service;

import com.alibaba.fastjson.JSON;
import com.example.riskcontrol.dao.RedisDao;
import com.example.riskcontrol.mapper.BlackListMapper;
import com.example.riskcontrol.model.BlackList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Service
public class BlackListService {

    private String channel = this.getClass().getName();

    private Map<String, BlackList> blackListMap;

    private static Logger logger = LoggerFactory.getLogger(BlackListService.class);

    @Autowired
    private BlackListMapper blackListMapper;

    @Autowired
    private RedisDao redisDao;


    @PostConstruct
    public void init() {
        new Thread() {
            @Override
            public void run() {
                redisDao.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String pchannel, String message) {
                        logger.info("redis通知，channel={},message={}", pchannel, message);
                        if (channel.equals(pchannel)) {
                            updateCache();
                        }
                    }
                }, channel);
            }
        }.start();

        updateCache();
    }

    public List<BlackList> queryAll() {
        return blackListMap.values().stream().collect(Collectors.toList());
    }


    public void updateCache() {
        List<BlackList> blackLists = blackListMapper.queryAll();
        Map<String, BlackList> tempMap = new ConcurrentHashMap<>();
        for (BlackList blackList : blackLists) {
            tempMap.put(blackList.getValue(), blackList);
        }
        blackListMap = tempMap;
        logger.info("黑名单缓存更新成功");
    }


    public void add(BlackList blackList) {
        if (blackListMapper.query(blackList) != null) {
            logger.info("黑名单已存在，{}", JSON.toJSONString(blackList));
        } else {
            blackListMapper.add(blackList);
            logger.info("黑名单添加成功，{}", JSON.toJSONString(blackList));
        }
    }


    public void pub(BlackList blackList) {
        add(blackList);
        redisDao.publish(this.channel, "");
    }

    public boolean contain(BlackList.EnumDimension enumDimension, BlackList.EnumType enumType, String value) {
        BlackList blackList1 = blackListMap.get(value);
        return blackList1 == null ? false :
                (blackList1.getDimension().equals(enumDimension) && blackList1.getType().equals(enumType));
    }

}
