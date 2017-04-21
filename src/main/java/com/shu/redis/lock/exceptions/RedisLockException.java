package com.shu.redis.lock.exceptions;

import lombok.Getter;
import lombok.Setter;

/**
 * 自定义redis锁异常类
 */
@Getter
@Setter
public class RedisLockException extends RuntimeException {
    /**
     * 异常码
     */
    private String code;

    /**
     * 异常描述
     */
    private String desc;


    public RedisLockException(){

    }

    public RedisLockException(ExceptionEnum exceptionEnum){
        super(exceptionEnum.getCode());
        this.code = exceptionEnum.getCode();
        this.desc = exceptionEnum.getDesc();
    }
}
