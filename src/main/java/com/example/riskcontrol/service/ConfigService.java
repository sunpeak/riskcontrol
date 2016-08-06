package com.example.riskcontrol.service;

import com.alibaba.fastjson.JSON;
import com.example.riskcontrol.dao.RedisDao;
import com.example.riskcontrol.mapper.ConfigMapper;
import com.example.riskcontrol.model.Config;
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
public class ConfigService {


    private String channel = this.getClass().getName();

    private Map<String, Config> configMap;

    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigMapper configMapper;

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

    public List<Config> queryAll() {
        return configMap.values().stream().collect(Collectors.toList());
    }


    public void updateCache() {
        List<Config> configs = configMapper.queryAll();
        Map<String, Config> tempMap = new ConcurrentHashMap<>();
        for (Config config : configs) {
            tempMap.put(config.getKey(), config);
        }
        configMap = tempMap;
        logger.info("配置缓存更新成功");
    }


    public void update(Config config) {
        configMapper.update(config);
        logger.info("配置更新成功，{}", JSON.toJSONString(config));
    }


    public void pub(Config config) {
        update(config);
        redisDao.publish(this.channel, "");
    }

    public String query(String key) {
        Config config = configMap.get(key);
        return config == null ? null : config.getValue();
    }


}
