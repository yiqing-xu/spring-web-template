package com.xyq.tweb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基于jackson的序列化工具
 * </p>
 * @author 徐益庆
 * @since 2022/2/24
 */
public class JacksonHelper {

    private static ObjectMapper MAPPER;

    static {
        try {
            MAPPER = SpringContextUtils.getBean(ObjectMapper.class);
        } catch (NoSuchBeanDefinitionException e) {
            MAPPER = new ObjectMapper();
        }
    }

    public static String toString(Object data) {
        String string = null;
        try {
            string = MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException ignore) {
        }
        return string;
    }

    public static <T> T parseObject(String jsonData, Class<T> beanType) {
        T bean = null;
        try {
            bean = MAPPER.readValue(jsonData, beanType);
        } catch (JsonProcessingException ignore) {
        }
        return bean;
    }

    public static <T> List<T> parseList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructCollectionType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (JsonProcessingException ignore) {
            return new ArrayList<>(0);
        }
    }

    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (JsonProcessingException ignore) {
            return new HashMap<>(0);
        }
    }

    public static void main(String[] args) {
        String s = "[{\"yes\": true}]";
        List<TT> tts = parseList(s, TT.class);
        System.out.println(tts);
    }

    public static class TT {
        private Boolean yes;

        public Boolean getYes() {
            return yes;
        }

        public void setYes(Boolean yes) {
            this.yes = yes;
        }

        @Override
        public String toString() {
            return "TT{" +
                    "yes=" + yes +
                    '}';
        }
    }

}
