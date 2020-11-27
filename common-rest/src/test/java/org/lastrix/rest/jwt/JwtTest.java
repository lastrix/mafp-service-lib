package org.lastrix.rest.jwt;


import com.auth0.jwt.JWT;
import org.junit.jupiter.api.Test;
import org.lastrix.jwt.Jwt;
import org.lastrix.jwt.JwtSecret;
import org.lastrix.jwt.UserType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class JwtTest {
    @Test
    public void testJwt() {
        var jwtSecret = new JwtSecret();
        var expiration = Instant.now().plus(12, ChronoUnit.HOURS);
        var token = JWT.create()
                .withExpiresAt(Date.from(expiration))
                .withIssuer("mafp.lastrix.org")
                .withClaim(Jwt.CLAIM_USER_TYPE, UserType.PERSON.name())
                .withClaim(Jwt.CLAIM_USER_ID, new UUID(1L, 1L).toString())
                .withArrayClaim(Jwt.CLAIM_ROLES, new String[]{"User"})
                .sign(jwtSecret.getAlgorithm());
        System.out.println("PERSON: " + token);
        JWT.require(jwtSecret.getAlgorithm())
                .acceptExpiresAt(0)
                .withIssuer("mafp.lastrix.org")
                .build()
                .verify(token);
        var jwt = new Jwt(JWT.decode(token));

        System.out.println(jwt.getUserId());
    }

    @Test
    public void testJwtSrv() {
        var jwtSecret = new JwtSecret();
        var expiration = Instant.now().plus(12, ChronoUnit.HOURS);
        var token = JWT.create()
                .withExpiresAt(Date.from(expiration))
                .withIssuer("mafp.lastrix.org")
                .withClaim(Jwt.CLAIM_USER_TYPE, UserType.SRV.name())
                .withClaim(Jwt.CLAIM_USER_ID, new UUID(Long.MAX_VALUE, 3L).toString())
                .withArrayClaim(Jwt.CLAIM_ROLES, new String[]{"ServiceUser"})
                .sign(jwtSecret.getAlgorithm());
        System.out.println("SRV: " + token);
        JWT.require(jwtSecret.getAlgorithm())
                .acceptExpiresAt(0)
                .withIssuer("mafp.lastrix.org")
                .build()
                .verify(token);
        var jwt = new Jwt(JWT.decode(token));

        System.out.println(jwt.getUserId());
    }
}
