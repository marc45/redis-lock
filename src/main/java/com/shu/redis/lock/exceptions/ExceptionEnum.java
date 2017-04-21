package com.shu.redis.lock.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常枚举
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {

    COMPETE_REDIS_KEY_DEFEAT("COMPETE_REDIS_KEY_DEFEAT","获取redis锁失败");

    /**
     * 异常枚举码
     */
    private String code;

    /**
     * 异常枚举描述
     */
    private String desc;

    public static ExceptionEnum gainExceptionEnum(String code){
        for(ExceptionEnum exceptionEnum:ExceptionEnum.values()){
            if(exceptionEnum.code.equals(code)){
                return exceptionEnum;
            }
        }
        return null;
    }
}
