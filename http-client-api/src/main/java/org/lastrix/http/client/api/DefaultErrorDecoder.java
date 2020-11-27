package org.lastrix.http.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.lastrix.rest.Rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class DefaultErrorDecoder implements ErrorDecoder {
    private final ObjectMapper mapper;
    private final String serviceName;

    @SuppressWarnings("unchecked")
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.body() == null) {
            return buildDefaultException(methodKey, response);
        }
        try (InputStream is = response.body().asInputStream()) {
            var text = IOUtils.toString(is, StandardCharsets.UTF_8);
            var rest = mapper.readValue(text, Rest.class);
            if (rest.isSuccess()) {
                throw new IllegalStateException();
            }
            if (rest.getErrors() == null || rest.getErrors().isEmpty()) {
                return buildDefaultException(methodKey, response);
            }
            rest.getErrors().forEach(e -> log.error("Got error from remote: {}\r\n{}", methodKey, e));

            List<String> errors = rest.getErrors();
            return new IllegalStateException(errors.get(0));
        } catch (IOException e) {
            return buildDefaultException(methodKey, response);
        }
    }

    private Exception buildDefaultException(String methodKey, Response response) {
        return new IllegalStateException("Failed to contact: " + serviceName + System.lineSeparator()
                + "Method: " + methodKey + System.lineSeparator()
                + "Status: " + response.status() + System.lineSeparator()
                + "Path: " + response.request().url());
    }

}
