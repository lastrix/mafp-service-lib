package org.lastrix.perf.tester;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public final class DummyPerfSuite implements PerfSuite {
    private final SecureRandom random = new SecureRandom();

    @Override
    public void init() {
        log.info("Initializing...");
    }

    @Override
    public void next() throws Exception {
        log.trace("Test!");
        Thread.sleep(100 + random.nextInt(100) - 50);
    }

    @Override
    public void close() {
        log.info("Closing...");
    }
}
