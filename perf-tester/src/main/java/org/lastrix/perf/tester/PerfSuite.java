package org.lastrix.perf.tester;

public interface PerfSuite {
    void init();

    void next() throws Exception;

    void close();
}
