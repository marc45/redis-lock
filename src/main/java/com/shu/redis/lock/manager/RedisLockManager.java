package com.shu.redis.lock.manager;

import com.shu.redis.lock.exceptions.ExceptionEnum;
import com.shu.redis.lock.exceptions.RedisLockException;
import com.shu.redis.lock.utils.Constant;
import com.shu.redis.lock.utils.RedisKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * redis 锁
 * <p>
 * <p>
 * 1、锁定
 * 2、释放锁
 * </p>
 */
@Slf4j
@Component
public class RedisLockManager {

    /**
     * redis 服务
     */
    @Autowired
    private RedisManager redisManager;

    /**
     * 锁定
     *
     * @param key
     */
    public boolean lock(RedisKeyEnum redisKeyEnum, String... key) {
        String lockKey = buildRedisKey(redisKeyEnum, key);
        if (redisManager.setIfNotExist(lockKey, Constant.THIRTY_MINUTES_TIME)) {
            return true;
        }
        log.error("获取锁异常:{}", lockKey);
        throw new RedisLockException(ExceptionEnum.COMPETE_REDIS_KEY_DEFEAT);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void release(RedisKeyEnum redisKeyEnum, String... key) {
        String lockKey = buildRedisKey(redisKeyEnum, key);
        redisManager.deleteObject(lockKey);
    }

    /**
     * 获取锁，根据key值进行锁定
     *
     * @param keyTarget redis锁的key值
     * @return 锁定结果
     */
    public boolean lockRedisKey(String keyTarget) {

        return lock(RedisKeyEnum.REDIS_KEY_TYPE_ONE, keyTarget);
    }

    /**
     * 释放锁，根据key值进行释放
     *
     * @param keyTarget redis锁的key值
     * @return 释放结果
     */
    public boolean releaseRedisKey(String keyTarget) {
        release(RedisKeyEnum.REDIS_KEY_TYPE_ONE, keyTarget);
        return true;
    }


    /**
     * 组装redis的key值
     *
     * @param redisKeyEnum key前缀
     * @param params 组成key的参数
     * @return key
     */
    public static String buildRedisKey(RedisKeyEnum redisKeyEnum, String ...params){
        StringBuffer redisKey = new StringBuffer(redisKeyEnum.getCode());
        for(String param:params){
            redisKey.append(Constant.COLON+param);
        }
        log.debug("生成redis中Key的参数:{}", redisKey.toString());
        return redisKey.toString();
    }
}
