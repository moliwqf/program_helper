package com.moli.redis.utils;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author moli
 * @time 2024-03-23 17:17:13
 * @description 类相关工具信息
 */
public class ClassUtil {

    /**
     * 获取标有注解的类信息
     */
    public static List<Class<?>> getAnnoClasses(Class<? extends Annotation> annotationClass, String packPath) {
        Reflections f = new Reflections(packPath);
        Set<Class<?>> set = f.getTypesAnnotatedWith(annotationClass);
        return new ArrayList<>(set);
    }

    /**
     * 将String转为 T
     */
    public static <T> T stringCastToType(String val, Class<T> clazz) {
        T ret = null;
        try {
            Constructor<T> tConstructor = clazz.getConstructor(String.class);
            tConstructor.setAccessible(true);
            ret = tConstructor.newInstance(val);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }
}
