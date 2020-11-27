package org.lastrix.http.client.api;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public final class PlainHttpErrorDecoder implements ErrorDecoder {
    private final String serviceName;

    @Override
    public Exception decode(String s, Response response) {
        return new IllegalStateException("Failed to contact: " + serviceName + System.lineSeparator()
                + "Method: " + s + System.lineSeparator()
                + "Status: " + response.status() + System.lineSeparator()
                + "Body: " + bodyAsString(response.body()));
    }

    private String bodyAsString(Response.Body body) {
        if (body == null) {
            return "[no body]";
        }
        try (InputStream is = new BufferedInputStream(body.asInputStream())) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "[" + e.getMessage() + "]";
        }
    }
}
