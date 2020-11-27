package org.lastrix.http.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.*;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractHttpClientAutoConfiguration {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @SuppressWarnings("unused") // API
    protected final <T> T buildClient(String url, Class<T> clientClass, String serviceName) {
        return buildClient(url, clientClass, new DefaultErrorDecoder(mapper, serviceName));
    }

    protected final <T> T buildClient(String url, Class<T> clientClass, ErrorDecoder errorDecoder) {
        return feignBuilder()
                .requestInterceptor(jwtAuthInterceptor())
                .errorDecoder(errorDecoder)
                .logger(new Slf4jLogger(clientClass))
                .target(clientClass, url);
    }

    protected Feign.Builder feignBuilder() {
        return Feign.builder()
                .client(new Client.Default(sslSocketFactory(), null))
                .encoder(new SpringFormEncoder(new SpringEncoder(messageConverters)))
                .decoder(new JacksonDecoder(mapper))
                .retryer(getRetryer())
                .contract(new SpringMvcContract())
                .logLevel(getLogLevel());
    }

    protected Retryer getRetryer() {
        return new Retryer.Default();
    }

    protected Logger.Level getLogLevel() {
        return Logger.Level.BASIC;
    }

    protected abstract RequestInterceptor jwtAuthInterceptor();

    protected SSLSocketFactory sslSocketFactory() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new NonValidatingTrustManager()}, null);
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SSL Socket Factory!", e);
        }
    }

    private static class NonValidatingTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }

}
