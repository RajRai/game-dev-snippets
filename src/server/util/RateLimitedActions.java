package server.util;

import server.Processing;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RateLimitedActions {

    private static final HashMap<Object, List<Instant>> actionDataMap = new HashMap<>();

    public static boolean processAction(Object identifier, int allowedActions, Duration expireAfter, Consumer<Integer> action, Consumer<Duration> onFail) {
        List<Instant> actionDataList = actionDataMap.computeIfAbsent(identifier, key -> new ArrayList<>());

        // Prune expired actions
        pruneActions(actionDataList);

        int remainingActions = allowedActions - actionDataList.size();

        if (remainingActions > 0) {
            actionDataList.add(Processing.currentInstant.plus(expireAfter));
            action.accept(remainingActions - 1); // Pass remainingActions - 1 to the consumer
            return true;
        } else {
            if (onFail != null) {
                Duration timeUntilAction = calculateTimeUntilAction(actionDataList, allowedActions);
                onFail.accept(timeUntilAction);
            }
            return false;
        }
    }

    public static void resetActions(Object identifier) {
        actionDataMap.remove(identifier);
    }

    private static Duration calculateTimeUntilAction(List<Instant> actionDataList, int limit) {
        if (actionDataList.size() - limit < 0 || limit < 0){
            return Duration.ZERO;
        }
        Instant instant = actionDataList.stream()
            .sorted(Instant::compareTo)
            .toList()
            .get(actionDataList.size()-limit);
        return Duration.between(Processing.currentInstant, instant);
    }

    /*
     * Map cleaning
     */

    static {
        TaskScheduler.scheduleDailyQuietHours(RateLimitedActions::pruneAll);
    }

    private static void pruneAll(){
        actionDataMap.values().forEach(RateLimitedActions::pruneActions);
        actionDataMap.entrySet().removeIf(entry -> entry.getValue().size() == 0);
    }

    private static void pruneActions(List<Instant> data){
        data.removeIf(instant -> instant.isBefore(Processing.currentInstant));
    }

    /*
     * Example
     */
    public static void main(String[] args) {
        Object playerId = "player123"; // Replace with an appropriate identifier

        // Simulate processing actions
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(Misc.random(3000)); // Sleep for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Processing.currentInstant = Instant.now();
            RateLimitedActions.processAction(
                playerId,
                3, // 3 actions...
                Duration.ofSeconds(10), // ...every 10 seconds
                remaining -> {
                    System.out.println("Action performed. Remaining actions: " + remaining);
                },
                remainingTime -> {
                    System.out.println("Action rate-limited. Time remaining: " + remainingTime.toSeconds() + " seconds");
                }
            );
        }
    }
}