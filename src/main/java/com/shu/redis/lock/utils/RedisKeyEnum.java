package com.shu.redis.lock.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * redis key 枚举
 * 更具不同的业务制定不同的Redis锁key值的前缀
 */
@Getter
@AllArgsConstructor
public enum RedisKeyEnum {

    /**
     * 业务类型1的锁前缀
     */
    REDIS_KEY_TYPE_ONE("REDIS_KEY_TYPE_ONE","业务类型1的锁前缀"),

    /**
     * 业务类型2的锁前缀
     */
    REDIS_KEY_TYPE_TWO("REDIS_KEY_TYPE_TWO","业务类型2的锁前缀");


    private String code;

    private String desc;
}
