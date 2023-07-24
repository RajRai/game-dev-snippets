package server.util;

import server.Server;
import server.model.players.Player;

import java.util.HashMap;
import java.util.function.Consumer;

public class CooldownActions {
    private static final HashMap<Object, Long> cooldownSet = new HashMap<>();

    public static boolean processAction(Object identifier, long cooldownMs, Runnable action, Consumer<Long> onFail) {
        if (cooldownSet.getOrDefault(identifier, 0L) + cooldownMs > Server.currentTime) {
            onFail.accept(cooldownSet.get(identifier));
            return false;
        }

        if (cooldownMs > 0){
            cooldownSet.put(identifier, Server.currentTime);
        }

        action.run();
        return true;
    }

    public static boolean processAction(Player c, Object identifier, long cooldownMs, Runnable action){
        return processAction(
            identifier, cooldownMs, action,
            (remaining) -> c.sendMessage("You've done that too recently. Time remaining: <col=ff0000>" + TextUtils.formatMillisTime(remaining))
        );
    }

    // todo: maybe there's a better way to remove stale entries without a lot of needless iteration
    static {
        TaskScheduler.scheduleDailyQuietHours(CooldownActions::prune);
    }

    private static void prune() {
        cooldownSet.entrySet().removeIf(entry -> entry.getValue() < Server.currentTime);
    }
}
