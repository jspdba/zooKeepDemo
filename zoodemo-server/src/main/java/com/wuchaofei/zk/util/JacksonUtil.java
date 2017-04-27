package com.wuchaofei.zk.util;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: admin
 * Date: 13-6-18
 * Time: 上午11:28
 * To change this template use File | Settings | File Templates.
 */
public class JacksonUtil {
    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.enableDefaultTyping();
    }

    /**
     * 对象转json字符串
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        try {
            mapper.getSerializerProvider().setNullValueSerializer(
                    new JsonSerializer<Object>() {
                        @Override
                        public void serialize(Object value, JsonGenerator jgen,
                                              SerializerProvider provider) throws IOException,
                                JsonProcessingException {
                            jgen.writeString("");
                        }
                    });
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json字符串转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toObject(String json, Class<T> type) throws IOException {
        if (json == null) {
            return null;
        }
        return mapper.readValue(json, type);

    }

    public static Map<String, Object> toMap(String json) throws IOException {
        Map<String, Object> mapclass = new HashMap<String, Object>();
        return toObject(json, mapclass.getClass());
    }


    /**
     * json字符串转对象列表
     *
     * @param json 字符串
     * @param type List<Bean>中Bean的类型
     * @return
     */
    public static <T> T toList(String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            return mapper.readValue(json, getCollectionType(ArrayList.class, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String instanceofTo(String json) throws IOException {
        Object object = toObject(json, Object.class);
        if (object instanceof List) {
            return "list";
        }
        if (object instanceof Map) {
            return "map";
        }
        return "";
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return java 类型
     */
    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

    }

    /**
     * json字符串转JSONNode
     *
     * @param json
     * @return
     */
    public static JsonNode toJsonNode(String json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toJsonString(JsonNode jsonNode, String key, Class<T> type) {
        return JacksonUtil.toObject(jsonNode.findValue(key), type);
    }

    /**
     * JsonNode转对象
     *
     * @param node json节点
     * @param type 对象类型
     * @return
     */
    public static <T> T toObject(JsonNode node, Class<T> type) {
        if (node == null) {
            return null;
        }
        try {
            return mapper.readValue(node, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JsonNode转对象列表
     *
     * @param node json节点
     * @param type List<Bean>中Bean的类型
     * @return
     */
    public static <T> T toList(JsonNode node, Class<T> type) {
        if (node == null) {
            return null;
        }
        try {
            return mapper.readValue(node, getCollectionType(ArrayList.class, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
