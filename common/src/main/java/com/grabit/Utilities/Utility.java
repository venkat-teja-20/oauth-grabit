package com.grabit.Utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Utility {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final ObjectMapper SNAKE_CASE_OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object o) {
        try {
            OBJECT_MAPPER.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }
    }

//    public static ErrorObject buildErrorObject(String code, String message, int httpStatusCode, String service) {
//        ErrorObject errorObject = new ErrorObject();
//        errorObject.setErrorMsg(new APIError(code,message));
//        errorObject.setHttpCode(httpStatusCode);
//        errorObject.setService(service);
//        return errorObject;
//    }

    public static String toJsonSnakeCase(Object o) {
        try {
            return SNAKE_CASE_OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }
    }

    public static Boolean isNullOrEmpty(Object o) {
        return o == null || o.toString().trim().isEmpty();
    }

    public static Boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}
