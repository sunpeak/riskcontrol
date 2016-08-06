package com.example.riskcontrol.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Resource;

/**
 * Created by sunpeak on 2016/8/6.
 */
@Repository
public class RedisDao {

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;


    /**
     * 发布消息到指定的频道
     *
     * @param channel
     * @param message
     */
    public void publish(final String channel, final String message) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(final RedisConnection connection)
                    throws DataAccessException {
                ((Jedis) connection.getNativeConnection()).publish(channel, message);
                return null;
            }
        });
    }

    /**
     * 订阅给定的一个或多个频道的信息
     *
     * @param jedisPubSub
     * @param channels
     */
    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                ((Jedis) connection.getNativeConnection()).subscribe(jedisPubSub, channels);
                return null;
            }
        });
    }

    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本
     *
     * @param script
     * @param <T>
     * @return
     */
    public <T> T scriptLoad(final String script) {
        return (T) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return ((Jedis) connection.getNativeConnection()).scriptLoad(script);
            }
        });
    }


    /**
     * 对 Lua 脚本进行求值
     *
     * @param sha
     * @param keycount
     * @param args
     * @param <T>
     * @return
     */
    public <T> T evalsha(final String sha, final int keycount, final String... args) {
        return (T) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return ((Jedis) connection.getNativeConnection()).evalsha(sha, keycount, args);
            }
        });
    }


}
