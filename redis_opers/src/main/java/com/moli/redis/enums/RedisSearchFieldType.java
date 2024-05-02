package com.moli.redis.enums;

import org.redisson.api.search.index.FieldIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author moli
 * @time 2024-03-22 15:21:26
 * @description redis-search 字段类型转换
 */
public enum RedisSearchFieldType {

    /**
     * 文本类型 - TextField
     */
    STRING,

    /**
     * 数字类型 - NumericField
     */
    NUMERIC,

    /**
     * 根据类型进行自动匹配
     */
    AUTO;

    private static final List<String> PARAMS_TYPE = new ArrayList<String>(){{
        add("java.lang.String");
        add("java.lang.Char");
        add("Char");
    }};

    public static RedisSearchFieldType getType(String name) {
        if (PARAMS_TYPE.contains(name)) {
            return STRING;
        } else {
            return NUMERIC;
        }
    }

    public static FieldIndex getFieldIndex(String type, String fieldName) {
        RedisSearchFieldType fieldType = getType(type);
        return getFieldIndex(fieldType, fieldName);
    }

    public static FieldIndex getFieldIndex(RedisSearchFieldType type, String fieldName) {
        FieldIndex ret = null;
        switch (type) {
            case NUMERIC:
                ret = FieldIndex.numeric("$.." + fieldName).as(fieldName);
                break;
            case STRING:
            default:
                ret = FieldIndex.text("$.." + fieldName).as(fieldName);
                break;
        }
        return ret;
    }
}
