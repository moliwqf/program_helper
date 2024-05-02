package com.moli.redis.query;

import com.moli.redis.enums.RedisSearchFieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author moli
 * @time 2024-03-22 15:35:32
 * @description 给index 创建时使用
 */
@Data
@AllArgsConstructor
public class DocumentFieldInfo {

    /**
     * 字段类型 type
     */
    private RedisSearchFieldType fieldType;

    /**
     * redis中存储的 field - value 中的 field
     */
    private String fieldName;
}
