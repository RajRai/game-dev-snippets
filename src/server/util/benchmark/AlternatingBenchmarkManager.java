package server.util.benchmark;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class AlternatingBenchmarkManager<T, R> {
    private final Function<T, R> baseline;
    private final BasicBenchmarkManager baselineManager = new BasicBenchmarkManager();
    private final Function<T, R> test;
    private final BasicBenchmarkManager testManager = new BasicBenchmarkManager();

    private boolean checkBaseline = false;

    public AlternatingBenchmarkManager(Function<T, R> baseline, Function<T, R> test) {
        this.baseline = baseline;
        this.test = test;
    }

    public R benchmark(T params) {
        AtomicReference<R> result = new AtomicReference<>();
        if (checkBaseline){
            baselineManager.benchmark(() -> result.set(baseline.apply(params)));
        } else {
            testManager.benchmark(() -> result.set(test.apply(params)));
        }
        checkBaseline = !checkBaseline;
        return result.get();
    }

    public void printMetrics() {
        baselineManager.printMetrics("Baseline:");
        testManager.printMetrics("Test:");
    }
}