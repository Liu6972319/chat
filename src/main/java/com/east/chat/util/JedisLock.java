package com.east.chat.util;

import cn.hutool.db.nosql.redis.RedisDS;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

public class JedisLock {


    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁
     * @param requestId 请求标识
     * @param millisecondsToExpire 超期时间
     * @return 是否获取成功
     */
    public static boolean tryLock( String lockKey, String requestId, long millisecondsToExpire) {
        Jedis jedis = RedisDS.create().getJedis();
        String result = jedis.set(lockKey,requestId, SetParams.setParams().nx().px(millisecondsToExpire));
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseLock( String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Jedis jedis = RedisDS.create().getJedis();
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
