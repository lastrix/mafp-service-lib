package org.lastrix.perf.tester;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class PerfTester {
    private static final int MAX_TEST_THREADS = Integer.parseInt(System.getProperty("perf.tester.test.threads.max", "4"));
    private static final int MIN_TEST_THREADS = Integer.parseInt(System.getProperty("perf.tester.test.threads.min", "1"));
    private static final int WARMUP_ROUNDS = Integer.parseInt(System.getProperty("perf.tester.warmup.round.count", "1"));
    private static final int TEST_ROUNDS = Integer.parseInt(System.getProperty("perf.tester.test.round.count", "1"));
    private static final Duration WARMUP_ROUND_TIME = Duration.parse(System.getProperty("perf.tester.warmup.round.time", "PT1M"));
    private static final Duration TEST_ROUND_TIME = Duration.parse(System.getProperty("perf.tester.test.round.time", "PT1M"));

    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Single argument expected: PerfSuite class fqn");
        new PerfTester(args[0]).test();
    }

    public PerfTester(String suiteFqn) {
        try {
            var clazz = Class.forName(suiteFqn);
            if (!PerfSuite.class.isAssignableFrom(clazz))
                throw new IllegalArgumentException("Not an PerfSuite: " + clazz.getTypeName());
            suite = (PerfSuite) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize suite: " + suiteFqn, e);
        }
    }

    private final PerfSuite suite;
    private final ForkJoinPool pool = new ForkJoinPool(MAX_TEST_THREADS);

    private void test() {
        suite.init();
        try {
            for (int i = 0; i < WARMUP_ROUNDS; i++) warmup();
            for (int i = MIN_TEST_THREADS; i <= MAX_TEST_THREADS; i++) {
                log.info("Testing with {} workers", i);
                doTest(i);
            }
        } finally {
            suite.close();
        }
    }

    private void doTest(int workers) {
        List<TestResultSet> results = new ArrayList<>();
        try {
            for (int i = 0; i < TEST_ROUNDS; i++) {
                TestResultSet e = testImpl(workers);
                results.add(e);
                e.printStats();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted", e);
        }

    }

    private TestResultSet testImpl(int workers) throws InterruptedException {
        var set = new TestResultSet();
        var running = new AtomicBoolean(true);
        var list = new ArrayList<TestTask>();
        for (int i = 0; i < workers; i++) {
            TestTask task = new TestTask(running, suite, set);
            list.add(task);
            pool.submit(task);
        }
        var timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                set.nextSecondInfo();
                list.forEach(TestTask::chunk);
            }
        }, 1000, 1000);
        try {
            Thread.sleep(TEST_ROUND_TIME.toMillis());
            timer.cancel();
            running.set(false);
            awaitTasksComplete();
        } finally {
            timer.cancel();
            running.set(false);
        }
        return set;
    }

    private void warmup() {
        try {
            warmupImpl();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted", e);
        }
    }

    private void warmupImpl() throws InterruptedException {
        var running = new AtomicBoolean(true);
        pool.submit(new WarmupTask(running, suite));
        try {
            Thread.sleep(WARMUP_ROUND_TIME.toMillis());
            running.set(false);
            awaitTasksComplete();
        } finally {
            running.set(false);
        }
    }

    private void awaitTasksComplete() throws InterruptedException {
        while (!pool.isQuiescent()) {
            Thread.sleep(50);
        }
    }

    @RequiredArgsConstructor
    private static final class WarmupTask implements Runnable {
        private final AtomicBoolean running;
        private final PerfSuite suite;

        @Override
        public void run() {
            try {
                while (running.get()) suite.next();
            } catch (Exception e) {
                log.error("Worker failed", e);
            }
        }
    }

    @Getter
    private static final class TestResultSet {
        private List<TestResult> results = new CopyOnWriteArrayList<>();
        private List<SecondInfo> secondInfos = new CopyOnWriteArrayList<>();

        public void add(TestResult result) {
            results.add(result);
        }

        public void addSecondInfo(TestResult result) {
            secondInfos.get(secondInfos.size() - 1).add(result);
        }

        public void nextSecondInfo() {
            secondInfos.add(new SecondInfo());
        }

        public void printStats() {
            results.sort(Comparator.comparingLong(TestResult::getRps));
            var gps = getPerfInfo();
            log.info("Global: {} {} {} {} {} {}",
                    gps.getAvgRps() * results.size(), gps.getMinRps() * results.size(), gps.getMaxRps() * results.size(),
                    gps.getAvgRt(), gps.getMinRt(), gps.getMaxRt()
            );

            var ps = secondInfos.stream().map(SecondInfo::getPerfPair).collect(Collectors.toList());
            var pMinRps = ps.stream().mapToLong(PerfPair::getRps).min().orElse(0L) * results.size();
            var pMaxRps = ps.stream().mapToLong(PerfPair::getRps).max().orElse(0L) * results.size();
            var pAvgRps = (long) ps.stream().mapToLong(PerfPair::getRps).average().orElse(0L) * results.size();
            var pMinRt = ps.stream().mapToDouble(PerfPair::getRt).min().orElse(0d);
            var pMaxRt = ps.stream().mapToDouble(PerfPair::getRt).max().orElse(0d);
            var pAvgRt = ps.stream().mapToDouble(PerfPair::getRt).average().orElse(0d);
            log.info("Per second - {} parts: {} {} {} {} {} {}", secondInfos.size(),
                    pAvgRps, pMinRps, pMaxRps, pAvgRt, pMinRt, pMaxRt
            );
            log.info("Per second rps: {}", ps.stream().map(PerfPair::getRps).map(Object::toString).collect(Collectors.joining(", ")));
            log.info("Per second rt: {}", ps.stream().map(PerfPair::getRt).map(Object::toString).collect(Collectors.joining(", ")));
        }

        public PerfInfo getPerfInfo() {
            var rpsStats = results.stream().map(TestResult::getRps).mapToLong(e -> e).summaryStatistics();
            var rtStats = results.stream().map(TestResult::getAvgRequestTime).mapToDouble(e -> e).summaryStatistics();
            return new PerfInfo(rpsStats.getMin(), rpsStats.getMax(), (long) rpsStats.getAverage(),
                    rtStats.getMin(), rtStats.getMax(), rtStats.getAverage());
        }
    }

    private static final class SecondInfo {
        private final List<TestResult> results = new CopyOnWriteArrayList<>();

        public void add(TestResult result) {
            results.add(result);
        }

        public PerfPair getPerfPair() {
            var rpsStats = results.stream().map(TestResult::getRps).mapToLong(e -> e).average().orElse(0d);
            var rtStats = results.stream().map(TestResult::getAvgRequestTime).mapToDouble(e -> e).average().orElse(0d);
            return new PerfPair((long) rpsStats, rtStats);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static final class PerfInfo {
        private final long minRps;
        private final long maxRps;
        private final long avgRps;
        private final double minRt;
        private final double maxRt;
        private final double avgRt;
    }

    @RequiredArgsConstructor
    @Getter
    private static final class PerfPair {
        private final long rps;
        private final double rt;
    }

    @RequiredArgsConstructor
    @Getter
    private static final class TestResult {
        private final Duration duration;
        private final long requests;

        public long getRps() {
            var msTime = duration.toMillis() + duration.toNanosPart() * 10e-9;
            return (long) (requests / msTime * 1000);
        }

        public double getAvgRequestTime() {
            var msTime = duration.toMillis() + duration.toNanosPart() * 10e-9;
            return msTime / requests;
        }
    }

    @RequiredArgsConstructor
    private static final class TestTask implements Runnable {
        private final AtomicBoolean running;
        private final PerfSuite suite;
        private final TestResultSet set;
        private volatile boolean chunk = false;

        public void chunk() {
            chunk = true;
        }

        @Override
        public void run() {
            try {
                var start = Instant.now();
                var count = 0L;
                var chunkStamp = Instant.now();
                var chunkCount = 0L;
                while (running.get()) {
                    count++;
                    chunkCount++;
                    suite.next();
                    if (chunk) {
                        chunk = false;
                        var c = Instant.now();
                        set.addSecondInfo(new TestResult(Duration.between(chunkStamp, c), chunkCount));
                        chunkStamp = c;
                        chunkCount = 0L;
                    }
                }
                var elapsed = Duration.between(start, Instant.now());
                set.add(new TestResult(elapsed, count));
            } catch (Exception e) {
                log.error("Worker failed", e);
            }
        }
    }
}
