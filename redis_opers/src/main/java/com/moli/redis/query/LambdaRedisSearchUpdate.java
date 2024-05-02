package com.moli.redis.query;

import com.moli.redis.annotation.RedisSearch;
import com.moli.redis.annotation.DocumentField;
import com.moli.redis.entity.DocumentEntity;
import com.moli.redis.enums.RedisSearchFieldType;
import com.moli.redis.utils.RedisSearchClientUtil;
import io.redisearch.Schema;
import io.redisearch.client.Client;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.exceptions.JedisDataException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author moli
 * @time 2024-03-22 15:33:57
 * @description 更新操作
 */
public class LambdaRedisSearchUpdate<T> {

    /**
     * document实例
     */
    private final T t;
    /**
     * 对应的客户端连接
     */
    private Client client = null;

    /**
     * 初始化字段
     * @param t 查询的实体信息
     */
    public LambdaRedisSearchUpdate(T t) {
        this.t = t;
        String index = getIndex();
        this.client = RedisSearchClientUtil.getClient(index);

        Map<String, Object> info = null;
        try {
            info = client.getInfo();
            System.out.println(info);
        } catch (JedisDataException e) {
            if (e.getMessage().equals("Unknown index name")) {
                // 说明没有 这个 index 要创建
                if (!createIndex()) {
                    throw new RuntimeException("创建 索引失败");
                }
            }
        }
    }

    private boolean createIndex() {
        Schema schema = new Schema();
        List<DocumentFieldInfo> entityFields = getEntityFields();

        entityFields.forEach(ent -> {
            RedisSearchFieldType fieldType = ent.getFieldType();
            if (fieldType.equals(RedisSearchFieldType.STRING)) {
                schema.addTextField(ent.getFieldName(), 255);
            } else if (fieldType.equals(RedisSearchFieldType.NUMERIC)) {
                schema.addNumericField(ent.getFieldName());
            }
        });
        return client.createIndex(schema, Client.IndexOptions.Default());
    }

    private List<DocumentFieldInfo> getEntityFields() {

        List<DocumentFieldInfo> fieldsEntities = new ArrayList<>();

        // 拿到该类
        Class<?> clz = t.getClass();
        // 获取实体类的所有属性，返回Field数组
        Field[] fields = clz.getDeclaredFields();
        // 获取到所有的属性信息
        for (Field field : fields) {
            Type genericType = field.getGenericType();
            String name = field.getName();
            DocumentField annotation = field.getAnnotation(DocumentField.class);
            if (annotation == null) {
                RedisSearchFieldType parameterType = RedisSearchFieldType.getType(genericType.getTypeName());
                fieldsEntities.add(new DocumentFieldInfo(parameterType, name));
            } else {
                RedisSearchFieldType parameterType;
                if (annotation.exist()) {
                    String s = annotation.fieldName();
                    if (!StringUtils.isEmpty(s)) {
                        name = s;
                    }
                    if (annotation.fieldType().equals(RedisSearchFieldType.AUTO)) {
                        parameterType = RedisSearchFieldType.getType(genericType.getTypeName());
                    } else {
                        parameterType = annotation.fieldType();
                    }
                    fieldsEntities.add(new DocumentFieldInfo(parameterType, name));
                }
            }

        }
        if (CollectionUtils.isEmpty(fieldsEntities)) {
            throw new RuntimeException(clz.getName() + "中没有有效属性, 请重新定义");
        }
        return fieldsEntities;
    }

    private String getIndex() {
        RedisSearch annotation = t.getClass().getAnnotation(RedisSearch.class);
        if (null != annotation) {
            return annotation.index();
        }
        throw new RuntimeException(t.getClass().getName() + "没找到 @RedisSearch 注解");
    }

    public boolean insert(String docId, T t) {
        Map<String, Object> documentByEntity = getDocumentByEntity(t);
        return client.addDocument(docId, documentByEntity);
    }

    private Map<String, Object> getDocumentByEntity(T t) {
        Map<String, Object> document = new HashMap<>();
        // 拿到该类
        Class<?> clz = t.getClass();
        // 获取实体类的所有属性，返回Field数组
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            String key = null;
            Object value = null;

            field.setAccessible(true);
            try {
                value = field.get(t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            DocumentField annotation = field.getAnnotation(DocumentField.class);
            if (annotation == null) {
                key = field.getName();
                document.put(key, value);
            } else if (annotation.exist()) {
                String s = annotation.fieldName();
                if (!StringUtils.isEmpty(s)) {
                    key = s;
                } else {
                    key = field.getName();
                }
                document.put(key, value);
            }
        }

        if (CollectionUtils.isEmpty(document)) {
            throw new RuntimeException(clz.getName() + "中没有有效属性, 请重新定义");
        }
        return document;
    }

    public boolean delete(String docId) {
        return client.deleteDocument(docId);
    }

    public boolean update(String docId, T t) {
        Map<String, Object> documentByEntity = getDocumentByEntity(t);
        return client.updateDocument(docId, 1, documentByEntity);
    }

    public static void main(String[] args) {
        LambdaRedisSearchUpdate<DocumentEntity> update = new LambdaRedisSearchUpdate<>(new DocumentEntity());
        for (int i = 100; i < 200; i++) {
            update.insert("000" + i, new DocumentEntity((long) i, "t" + i + i + i + i, "b" + i + i + i, 1000 + i, 1000));
        }
    }
}
