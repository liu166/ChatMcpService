package com.rag.chatmcpservice.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        return mapper;
    }

    public static <T> List<T> fromJson(File jsonFIle, Class<T> clazz)  {
        try {
            List<T> items = mapper.readValue(Files.newInputStream(jsonFIle.toPath()), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
            return items;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonToBoject(String jsonString, Class<T> clazz)  {
        try {
            return mapper.readValue(jsonString,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseJson(Object obj) {
        try {
            if (obj instanceof String str) {
                // 字符串，直接当 JSON 解析
                return mapper.readTree(str);
            }
            // 其他对象，先序列化再解析
            return mapper.valueToTree(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse failed", e);
        }
    }

    public static String toJson(Object obj)  {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
