package com.moli.redis.utils;

import com.moli.redis.annotation.DocumentField;
import com.moli.redis.annotation.RedisSearch;
import com.moli.redis.enums.RedisSearchFieldType;
import org.redisson.api.RJsonBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.redisson.api.search.query.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.JacksonCodec;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author moli
 * @time 2024-03-23 09:59:16
 * @description 使用Redisson实现RedisSearch相关功能
 */
@Component
public class RedissonRedisSearchUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 是否存在该索引
     *
     * @param index 索引
     * @return true or false
     */
    public boolean indexExist(String index) {
        RSearch search = redissonClient.getSearch();
        try {
            search.info(index);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    /**
     * 创建索引
     * @param t Class
     * @param indexType 索引类型
     */
    public <T> void createIndex(Class<T> t, IndexType indexType) {
        // 1. 获取搜索信息
        RSearch search = getRSearch(t);
        // 2. 获取索引信息
        String index = index(t);
        // 3. 获取前缀信息
        String prefix = prefix(t);
        // 4. 获取数据字段信息
        FieldIndex[] fields = checkAndGetFIndex(t, indexType);
        // 5. 创建索引
        search.createIndex(
                index,
                IndexOptions.defaults().on(indexType).prefix(prefix),
                fields
        );
    }

    /**
     * 获取前缀
     */
    private <T> String prefix(Class<T> t) {
        // 1. 获取注解信息
        RedisSearch redisSearch = t.getAnnotation(RedisSearch.class);
        // 2. 判断是否存在注解信息
        if (Objects.isNull(redisSearch)) {
            throw new RuntimeException("请添加 @RedisSearch 注解后再使用该功能");
        }
        // 3. 如果没有指定prefix信息，默认为类名
        String prefix = redisSearch.prefix();
        if (StringUtils.isEmpty(prefix)) {
            String simpleName = t.getSimpleName();
            prefix = Character.toLowerCase(simpleName.indexOf(0)) + simpleName.substring(1);
        }
        return prefix + ":";
    }

    /**
     * 获取字段信息
     */
    private <T> FieldIndex[] checkAndGetFIndex(Class<T> c, IndexType indexType) {
        // 1. 创建返回值
        List<FieldIndex> ret = new ArrayList<>();
        // 2. 获取所有字段
        Field[] fields = c.getDeclaredFields();
        // 3. 遍历
        for (Field field : fields) {
            String fieldName = field.getName();
            Type type = field.getGenericType();
            switch (indexType) {
                case HASH:
                    ret.add(FieldIndex.text(fieldName));
                    break;
                case JSON:
                    FieldIndex fieldIndex = RedisSearchFieldType.getFieldIndex(type.getTypeName(), fieldName);
                    ret.add(fieldIndex);
                    break;
            }
        }
        if (ret.isEmpty()) {
            throw new RuntimeException(c.getSimpleName() + "中没有有效属性，请设置");
        }
        // 4. 返回值
        return ret.toArray(new FieldIndex[0]);
    }

    /**
     * 获取索引
     */
    public <T> String index(Class<T> t) {
        // 1. 获取注解信息
        RedisSearch redisSearch = t.getAnnotation(RedisSearch.class);
        // 2. 判断是否存在注解信息
        if (Objects.isNull(redisSearch)) {
            throw new RuntimeException("请添加 @RedisSearch 注解后再使用该功能");
        }
        return redisSearch.index();
    }

    /**
     * 获取索引类型
     */
    public <T> IndexType indexType(Class<T> t) {
        // 1. 获取注解信息
        RedisSearch redisSearch = t.getAnnotation(RedisSearch.class);
        // 2. 判断是否存在注解信息
        if (Objects.isNull(redisSearch)) {
            throw new RuntimeException("请添加 @RedisSearch 注解后再使用该功能");
        }
        return redisSearch.indexType();
    }

    /**
     * 获取返回值属性
     */
    public <T> ReturnAttribute[] returnAttributes(Class<T> c) {
        // 1. 创建返回值
        List<ReturnAttribute> ret = new ArrayList<>();
        // 2. 获取所有字段
        Field[] fields = c.getDeclaredFields();
        // 3. 遍历
        for (Field field : fields) {
            String fieldName = field.getName();
            DocumentField documentField = field.getAnnotation(DocumentField.class);
            if (documentField != null && indexType(c) == IndexType.HASH) {
                if (!documentField.exist()) continue;

                String name = documentField.fieldName();
                if (!StringUtils.isEmpty(name)) fieldName = name;

                ret.add(new ReturnAttribute(fieldName));
            }
            ret.add(new ReturnAttribute(fieldName));
        }
        if (ret.isEmpty()) {
            throw new RuntimeException(c.getSimpleName() + "中没有有效属性，请设置");
        }
        // 4. 返回值
        return ret.toArray(new ReturnAttribute[0]);
    }

    /**
     * 插入一条文档数据
     */
    public <T> boolean insert(T t) {
        // 1. 判断是否存在索引
        Class<?> c = t.getClass();
        IndexType indexType = indexType(c);
        if (indexExist(index(c))) {
            createIndex(c, indexType);
        }
        // 2. 从文档中提取主键信息
        String id = checkAndGetId(t);
        // 3. 从文档中提取前缀信息
        String prefix = prefix(t.getClass());
        // 4. 获取文档键
        String docId = prefix + id;
        // 5. 根据类型进行插入
        switch (indexType) {
            case HASH:
                return insertToHash(t, docId);
            case JSON:
                return insertToJson(t, docId);
        }
        return false;
    }

    /**
     * hash插入
     */
    private <T> boolean insertToHash(T t, String docId) {
        // 通过rMap添加元素
        RMap<String, String> rMap = redissonClient.getMap(docId, new CompositeCodec(StringCodec.INSTANCE, redissonClient.getConfig().getCodec()));
        setAttrs(t, rMap);
        return true;
    }

    /**
     * json插入
     */
    public <T> boolean insertToJson(T t, String docId) {
        RJsonBucket<T> rjb = redissonClient.getJsonBucket(docId, new JacksonCodec<T>((Class<T>) t.getClass()));
        rjb.set(t);
        return true;
    }

    /**
     * 如果是hash模式，设置属性
     */
    private <T> void setAttrs(T t, RMap<String, String> rMap) {
        // 1. 获取Class信息
        Class<?> clazz = t.getClass();
        // 2. 获取所有字段
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            DocumentField documentField = field.getAnnotation(DocumentField.class);
            String fieldName = field.getName();
            field.setAccessible(true);
            if (documentField != null) {
                if (!documentField.exist()) continue;

                String val = documentField.fieldName();
                if (!StringUtils.isEmpty(val)) {
                    fieldName = val;
                }
            }
            try {
                rMap.put(fieldName, field.get(t).toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 从数据中提取主键信息
     */
    private <T> String checkAndGetId(T t) {
        // 1. 创建返回值
        String ret = null;
        // 2. 获取Class信息
        Class<?> clazz = t.getClass();
        // 3. 获取所有字段
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            DocumentField documentField = field.getAnnotation(DocumentField.class);
            if (documentField == null) continue;

            if (documentField.id() && documentField.exist()) {
                field.setAccessible(true);
                Object val = null;
                try {
                    val = field.get(t);
                    ret = val.toString();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (Objects.nonNull(ret)) break;
        }

        if (StringUtils.isEmpty(ret)) {
            throw new RuntimeException("不存在主键属性，请使用 @DocumentField 注解指定主键信息");
        }
        return ret;
    }

    /**
     * 搜索
     * @param target 实例对象
     * @param keyword 查询的关键字
     */
    public <T> List<T> search(Class<T> target, String keyword) {
        // 1. 获取索引信息
        String index = index(target);
        // 2. 没有索引就创建
        if (indexExist(index)) createIndex(target, indexType(target));
        // 3. 获取搜索信息
        RSearch rSearch = getRSearch(target);
        SearchResult sr = rSearch.search(
                index,
                StringUtils.isEmpty(keyword) ? "*" : "*" + keyword + "*",
                QueryOptions.defaults()
                        .returnAttributes(returnAttributes(target))
        );
        try {
            return castDocToType(sr.getDocuments(), target);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取rs类型
     * @param target target -> indexType
     */
    private <T> RSearch getRSearch(Class<T> target) {
        IndexType indexType = indexType(target);
        RSearch rs = null;
        switch (indexType) {
            case HASH:
                rs = redissonClient.getSearch();
                break;
            case JSON:
                rs = redissonClient.getSearch(StringCodec.INSTANCE);
                break;
            default:
                break;
        }
        return rs;
    }

    /**
     * 将doc对象集合转为指定对象集合
     * @param documentList @see Document
     * @param target 指定对象Class信息
     */
    private <T> List<T> castDocToType(List<Document> documentList, Class<T> target) throws InstantiationException, IllegalAccessException {
        List<T> ret = new ArrayList<>();
        if (documentList == null || documentList.isEmpty()) return ret;

        for (Document document : documentList) {
            T targetObj = getTargetObj(document, target);
            ret.add(targetObj);
        }
        return ret;
    }

    /**
     * 将document实例转为T类型
     */
    private <T> T getTargetObj(Document document, Class<T> target) throws InstantiationException, IllegalAccessException {
        Map<String, Object> attrs = document.getAttributes();
        T targetObj = target.newInstance();

        Field[] fields = target.getDeclaredFields();
        for (Field field : fields) {
            DocumentField documentField = field.getAnnotation(DocumentField.class);
            String fieldName = field.getName();
            if (documentField != null) {
                if (!documentField.exist()) continue;
                if (!StringUtils.isEmpty(documentField.fieldName())) {
                    fieldName = documentField.fieldName();
                }
            }
            Class<?> fieldType = field.getType();
            String val = attrs.get(fieldName).toString();
            Object finalVal = ClassUtil.stringCastToType(val, fieldType);

            field.setAccessible(true);
            field.set(targetObj, finalVal);
        }
        return targetObj;
    }
}

