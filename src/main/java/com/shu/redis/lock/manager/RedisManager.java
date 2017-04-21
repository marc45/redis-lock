package com.shu.redis.lock.manager;

import com.alibaba.fastjson.JSONObject;
import com.shu.redis.lock.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis管理类
 */
@Slf4j
@Component
public class RedisManager {

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 查询redis数据库
     *
     * @param key 查询关键字
     * @return redis  返回对象
     */
    private String queryObjectByKey(final String key) {
        log.debug("redisManager queryObjectByKey request:{}", key);
        try {
            String resultStr = (String) redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] redisKey = redisTemplate.getStringSerializer().serialize(key);
                    byte[] value = connection.get(redisKey);
                    if (value == null) {
                        return null;
                    }
                    return redisTemplate.getStringSerializer().deserialize(value);
                }
            });
            log.debug("redisManager queryObjectByKey response:{}", resultStr);
            return resultStr;
        } catch (Exception e) {
            log.error("redis 查询异常:{}", e);
            return null;
        }
    }

    /**
     * 查询redis数据库,指定返回类型
     *
     * @param key   查询关键字
     * @param clazz 指定返回对象类型
     * @param <T>   返回对象类型,泛型
     * @return 返回对象
     */
    public <T> T queryObjectByKey(final String key, final Class<T> clazz) {
        log.debug("redisManager queryObjectByKey request:{}", key);
        String resultStr = queryObjectByKey(key);
        if (StringUtils.isBlank(resultStr)) {
            return null;
        }
        T value = JSONObject.parseObject(resultStr, clazz);
        log.debug("redisManager queryObjectByKey response:{}", value.toString());
        return value;
    }

    /**
     * 插入redis 数据库(默认保存一天)
     *
     * @param obj 保存对象
     * @param key 关键字
     * @return 对象类型, 泛型
     */
    public boolean insertObject(final Object obj, final String key) {
        return insertObject(obj, key, Constant.KEY_TIME);
    }

    /**
     * 插入redis 数据库,设置有效期
     *
     * @param obj     保存对象
     * @param key     关键字
     * @param timeout 有效期（秒）
     * @return 对象类型, 泛型
     */
    public boolean insertObject(final Object obj, final String key, final long timeout) {
        log.debug("redisManager insertObject request:key={},obj={}", key, obj.toString());
        try {
            final String value = toJSONString(obj);
            boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] redisKey = redisTemplate.getStringSerializer().serialize(key);
                    byte[] redisValue = redisTemplate.getStringSerializer().serialize(value);
                    connection.set(redisKey, redisValue);
                    if (timeout > 0) {
                        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
                    }
                    return true;
                }
            });
            log.debug("redisManager insertObject response：{}", result);
            return result;
        } catch (Exception e) {
            log.error("redis 新增异常:{}", e);
            return false;
        }
    }

    /**
     * 删除redis 保存对象
     *
     * @param key 查询关键字
     * @return boolean
     */
    public boolean deleteObject(final String key) {
        log.debug("redisManager deleteObject request:key={}", key);
        try {
            Long result = redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] redisKey = redisTemplate.getStringSerializer().serialize(key);
                    return connection.del(redisKey);
                }
            });
            log.debug("redisManager deleteObject response：{}", result);
            return result > 0;
        } catch (Exception e) {
            log.error("redis 删除异常:{}", e);
            return false;
        }
    }

    /**
     * 更新 redis
     *
     * @param obj  操作对象
     * @param keys keys数组
     * @return boolean
     */
    public boolean update(final Object obj, final String... keys) {
        for (String key : keys) {
            deleteObject(key);
            insertObject(obj, key);
        }
        return true;
    }

    /**
     * redis 值如果是对象转成json,如果是字符串不变
     *
     * @param obj 值
     * @return 值
     */
    private String toJSONString(Object obj) {
        final String value;
        if (obj instanceof String) {
            value = obj + "";
        } else {
            value = JSONObject.toJSONString(obj);
        }
        return value;
    }

    /**
     * 查询redis 数据库
     *
     * @param keyEnum 查询关键字
     * @param clazz   指定返回List内存放的对象类型
     * @param <T>     返回对象类型,集合泛型
     * @return List<T>      返回对象集合
     */
    public <T> List<T> queryListByKey(final String keyEnum, final Class<T> clazz) {
        log.debug("queryListByKey request：{}", keyEnum);

        String resultStr = queryObjectByKey(keyEnum);
        if (StringUtils.isBlank(resultStr)) {
            return null;
        }

        List<T> value = JSONObject.parseArray(resultStr, clazz);

        log.debug("queryListByKey response：{}", value);
        return value;
    }

    /**
     * 当且仅当 key 不存在，将 key 的值设为 value ，并返回true；
     * 若给定的 key 已经存在，则不做任何动作，并返回false。
     *
     * @param key     查询关键字
     * @param timeout 过期时间(MILLISECONDS)
     * @return redis        返回对象
     */
    public boolean setIfNotExist(final String key, final long timeout) {
        log.debug("lockRedis request:key={}", key);
        if (timeout <= 0) {
            log.warn("lockRedis Fail lock Time gt 0");
            return false;
        }
        try {
            boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    byte[] redisKey = redisTemplate.getStringSerializer().serialize(key);
                    Boolean lock = false;
                    try {
                        lock = connection.setNX(redisKey, new byte[1]);
                        log.debug("lockRedis Lock Result:{}", lock);
                        Boolean setTimeOutResult = redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
                        log.debug("lockRedis setTimeOutResult TimeOut:{} Result:{}", timeout, setTimeOutResult);
                        if (lock && setTimeOutResult) {
                            return true;
                        }
                        if (!setTimeOutResult) {
                            redisTemplate.delete(key);
                            return false;
                        }
                    } catch (Exception e) {
                        if (lock) {
                            redisTemplate.delete(key);
                        }
                        log.warn("lockRedis Fail Exception:{}", e);
                    }
                    return false;
                }
            });

            log.debug("lockRedis request Result:{}", result);
            return result;
        } catch (Exception e) {
            log.error("lockRedis exception, e:{}", e);
            return false;
        }
    }

}
