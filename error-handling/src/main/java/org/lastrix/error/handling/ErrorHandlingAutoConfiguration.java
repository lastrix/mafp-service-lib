package org.lastrix.error.handling;

import org.springframework.context.annotation.Bean;

public class ErrorHandlingAutoConfiguration {
    @Bean
    public RestErrorHandlerController restErrorHandlerController() {
        return new RestErrorHandlerController();
    }
}
