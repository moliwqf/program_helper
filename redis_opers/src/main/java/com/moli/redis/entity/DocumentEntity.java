package com.moli.redis.entity;
import com.moli.redis.annotation.RedisSearch;
import com.moli.redis.annotation.DocumentField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.redisson.api.search.index.IndexType;

import java.io.Serializable;

/**
 * @author moli
 * @time 2024-03-22 15:26:08
 * @description redis-search 搜索实体
 */
@Data
@RedisSearch(index = "article", prefix = "doc", indexType = IndexType.HASH)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEntity implements Serializable {

    /**
     * 唯一键
     */
    @DocumentField(id = true)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 收藏数
     */
    private Integer star;

    /**
     * 是否存储该字段到redis中
     */
    @DocumentField(exist = false)
    private Integer notExistField;
}
