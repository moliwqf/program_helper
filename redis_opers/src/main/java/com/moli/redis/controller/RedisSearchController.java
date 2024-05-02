package com.moli.redis.controller;

import com.moli.redis.entity.DocumentEntity;
import com.moli.redis.utils.RedissonRedisSearchUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.redisson.api.search.query.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author moli
 * @time 2024-03-23 10:52:20
 * @description redis-search测试处理器
 */
@RestController
public class RedisSearchController {

    @Resource
    private RedissonRedisSearchUtil redissonRedisSearchUtil;

    @Resource
    private RedissonClient redisson;

    @GetMapping("/redis-search/test")
    public String test(String keyword) {
        RBucket<String> chineseData = redisson.getBucket("chineseData");
        chineseData.set("你好");
        // 获取并打印中文数据
        return chineseData.get();
    }

    @GetMapping("/redis-search")
    public List<DocumentEntity> getDoc(@RequestParam("id") Long id,
                                       @RequestParam("content") String content,
                                       @RequestParam("key") String keyword,
                                       @RequestParam("title") String title) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(id);
        documentEntity.setTitle(title);
        documentEntity.setContent(content);
        documentEntity.setStar(1);
        redissonRedisSearchUtil.insert(documentEntity);
        return redissonRedisSearchUtil.search(DocumentEntity.class, keyword);
    }

    @GetMapping("/redis-search/map")
    public List<DocumentEntity> getDocMap(@RequestParam("id") Long id) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setId(id);
        documentEntity.setTitle("文章标题");
        documentEntity.setContent("文章内容");
        documentEntity.setStar(1);
        redissonRedisSearchUtil.insert(documentEntity);
        return redissonRedisSearchUtil.search(DocumentEntity.class, id.toString());
    }

    @GetMapping("/redis-search/add")
    public List<Document> getAddDocMap() {
        RMap<String, String> m = redisson.getMap("doc:1", new CompositeCodec(StringCodec.INSTANCE, redisson.getConfig().getCodec()));
        m.put("v1", "name1");
        m.put("v2", "name2");
        RMap<String, String> m2 = redisson.getMap("doc:2", new CompositeCodec(StringCodec.INSTANCE, redisson.getConfig().getCodec()));
        m2.put("v1", "name3");
        m2.put("v2", "name4");

        RSearch s = redisson.getSearch();
        s.createIndex("idx", IndexOptions.defaults()
                        .on(IndexType.HASH)
                        .prefix(Arrays.asList("doc:")),
                FieldIndex.text("v1"),
                FieldIndex.text("v2"));

        SearchResult r = s.search("idx", "*", QueryOptions.defaults()
                .returnAttributes(new ReturnAttribute("v1"), new ReturnAttribute("v2")));
        System.out.println(s.info("idx"));
        return r.getDocuments();
    }
}
