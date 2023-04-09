package server.model.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * WIP and untested
 */
public class ChallengeRequest {

    // player key is the player receiving requests
    public static HashMap<Player, List<ChallengeRequest>> pendingRequests = new HashMap<>();

    private final Consumer<ChallengeRequest> callback;
    public final long time = System.currentTimeMillis();
    public final Player from;
    public final Player to;

    public ChallengeRequest(Player from, Player to, String text, Consumer<ChallengeRequest> onAccept){
        to.sendMessage(from.playerName + ":" + text + ":chalreq");
        this.from = from;
        this.to = to;
        callback = onAccept;
        if (!pendingRequests.containsKey(to)){
            pendingRequests.put(to, new ArrayList<>());
        }
        pendingRequests.get(to).add(this);
    }

    public void process(){
        callback.accept(this);
    }

}
