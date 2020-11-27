package org.lastrix.error.handling;

import lombok.extern.slf4j.Slf4j;
import org.lastrix.rest.Rest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class RestErrorHandlerController {
    @ExceptionHandler
    public ResponseEntity<Rest<Void>> handleError(HttpServletRequest request, Throwable throwable) {
        log.error("from '{}' -> {} {}",
                StringUtils.isEmpty(request.getRemoteHost()) ? request.getRemoteAddr() : request.getRemoteHost(),
                request.getMethod(), request.getRequestURL(), throwable);

        return Rest.error(throwable.getMessage(), HttpStatus.OK);

    }
}
