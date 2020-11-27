package org.lastrix.rest;

import com.auth0.jwt.JWT;
import org.lastrix.jwt.Jwt;
import org.lastrix.jwt.JwtSecret;
import org.lastrix.jwt.UserType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public final class DefaultJwtTokenProvider implements JwtTokenProvider {

    private final JwtSecret jwtSecret;
    private final String srvId;
    private final Long lifetime;

    public DefaultJwtTokenProvider(JwtSecret jwtSecret, String srvId, Long lifetime) {
        this.jwtSecret = jwtSecret;
        this.srvId = srvId;
        this.lifetime = lifetime;
    }

    private final AtomicReference<TokenWithStamp> reference = new AtomicReference<>(null);

    @Override
    public String getToken() {
        var token = reference.get();
        if (token != null && token.getExpiration().isAfter(Instant.now())) return token.getToken();
        var expiration = Instant.now().plus(lifetime, ChronoUnit.SECONDS);
        var tokenString = "Bearer " + JWT.create()
                .withExpiresAt(Date.from(expiration))
                .withIssuer(JwtAutoConfiguration.MAFP_ISSUER)
                .withClaim(Jwt.CLAIM_USER_TYPE, UserType.SRV.name())
                .withClaim(Jwt.CLAIM_USER_ID, srvId)
                .withArrayClaim(Jwt.CLAIM_ROLES, new String[]{"ServiceUser"})
                .sign(jwtSecret.getAlgorithm());
        reference.compareAndSet(token, new TokenWithStamp(tokenString, expiration));
        return tokenString;
    }
}
