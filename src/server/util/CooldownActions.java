package server.util;

import server.model.players.Player;

import java.time.Duration;
import java.util.function.Consumer;

public class CooldownActions {
    public static boolean processAction(Object identifier, Duration duration, Runnable action, Consumer<Duration> onFail) {
        return RateLimitedActions.processAction(
            identifier,
            1,
            duration,
            remaining -> action.run(),
            onFail
        );
    }

    public static boolean processAction(Player c, Object identifier, Duration duration, Runnable action){
        return processAction(
            identifier, duration, action,
            (remaining) -> c.sendMessage("You've done that too recently. Time remaining: <col=ff0000>" + TextUtils.formatMillisTime(remaining.toMillis()))
        );
    }
}
