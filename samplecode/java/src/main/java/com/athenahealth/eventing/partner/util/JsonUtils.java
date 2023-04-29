package com.athenahealth.eventing.partner.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@UtilityClass
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public static String convertIntoJson(Object obj) {
        String jsonString = null;
        if(Objects.nonNull(obj)){
            try {
                jsonString = objectMapper.writeValueAsString(obj);
            } catch (IOException e) {
                log.error("Error into json convert ", e);
            }
        }
        return jsonString;
    }

    public static <T> T convertToObject(Class<T> cls, String jsonString) {
        try {
            return objectMapper.readValue(jsonString, cls);
        } catch (Exception e) {
            log.error("Error into object convert ", e);
            return null;
        }
    }

    public static <T> T convertToObject(TypeReference<T> clazz, String jsonString) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            log.error("Error into object convert ", e);
            return null;
        }
    }
}
