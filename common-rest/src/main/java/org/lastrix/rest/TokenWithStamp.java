package org.lastrix.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public final class TokenWithStamp {
    private final String token;
    private final Instant expiration;
}
