package org.lastrix.jwt;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class Jwt {
    public static final String CLAIM_USER_TYPE = "user-type";
    public static final String CLAIM_USER_ID = "user-id";
    public static final String CLAIM_ROLES = "roles";

    private final DecodedJWT decodedJWT;

    public UserType getUserType() {
        Claim claim = decodedJWT.getClaim(CLAIM_USER_TYPE);
        if (claim.isNull()) return UserType.NONE;
        return UserType.valueOf(claim.asString());
    }

    public UUID getUserId() {
        Claim claim = decodedJWT.getClaim(CLAIM_USER_ID);
        if (claim.isNull()) return null;
        return UUID.fromString(claim.asString());
    }

    public Set<String> getRoles() {
        Claim claim = decodedJWT.getClaim(CLAIM_ROLES);
        if (claim.isNull()) return Collections.emptySet();
        return Set.of(claim.asArray(String.class));
    }

}
