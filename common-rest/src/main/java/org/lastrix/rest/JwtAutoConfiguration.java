package org.lastrix.rest;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.lastrix.jwt.Jwt;
import org.lastrix.jwt.JwtSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class JwtAutoConfiguration {

    public static final String PREFIX = "Bearer ";
    public static final String MAFP_ISSUER = "mafp.lastrix.org";

    @Bean
    public JwtSecret jwtSecret() {
        return new JwtSecret();
    }

    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    public Jwt jwt(HttpServletRequest request, JwtSecret jwtSecret) {
        var token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!token.startsWith(PREFIX)) throw new IllegalArgumentException("Invalid jwt token passed: " + token);
        token = token.substring(PREFIX.length()).trim();
        JWT.require(jwtSecret.getAlgorithm())
                .acceptExpiresAt(0)
                .withIssuer(MAFP_ISSUER)
                .build()
                .verify(token);
        return new Jwt(JWT.decode(token));
    }

    @Bean
    public JwtTokenProvider srvJwtTokenProvider(
            @Value("${mafp.srv-id:7fffffff-ffff-ffff-0000-000000000000}") String srvId,
            @Value("${mafp.jwt.lifetime:300}") Long lifetime,
            JwtSecret jwtSecret) {
        return new DefaultJwtTokenProvider(jwtSecret, srvId, lifetime);
    }

}
