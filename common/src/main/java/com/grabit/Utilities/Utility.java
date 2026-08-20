package com.grabit.Utilities;


import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
public class Utility {
    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public static final ObjectMapper SNAKE_CASE_OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JacksonException e) {
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
        } catch (JacksonException e) {
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
