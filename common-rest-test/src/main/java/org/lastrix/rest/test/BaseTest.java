package org.lastrix.rest.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class BaseTest {
    @Autowired
    protected ObjectMapper objectMapper;

    protected final String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    protected final <T> T fromJson(String text, Class<T> tClass) {
        try {
            return objectMapper.readValue(text, tClass);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
