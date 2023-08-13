package server.util.benchmark;

import server.XLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BasicBenchmarkManager {
    private final List<Long> executionTimes = new ArrayList<>();

    public void benchmark(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        executionTimes.add(executionTime);
    }

    public void printMetrics(String prefix) {
        if (executionTimes.isEmpty()) {
            XLogger.getInstance().log(Level.INFO, "No benchmarks have been run.");
            return;
        }

        long minTime = executionTimes.get(0);
        long maxTime = executionTimes.get(0);
        long totalExecutionTime = 0;

        for (Long executionTime : executionTimes) {
            if (executionTime < minTime) {
                minTime = executionTime;
            }
            if (executionTime > maxTime) {
                maxTime = executionTime;
            }
            totalExecutionTime += executionTime;
        }

        double averageTime = (double) totalExecutionTime / executionTimes.size();

        String metrics = prefix + "\nBenchmark Metrics:\n" +
            "Total Runs: " + executionTimes.size() + "\n" +
            "Total Execution Time: " + totalExecutionTime + " nanoseconds\n" +
            "Minimum Execution Time: " + minTime + " nanoseconds\n" +
            "Maximum Execution Time: " + maxTime + " nanoseconds\n" +
            "Average Execution Time: " + averageTime + " nanoseconds";

        XLogger.getInstance().log(Level.INFO, metrics);
    }
}
