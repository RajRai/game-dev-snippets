package server.observers.players;

import server.model.players.Player;
import server.model.players.skills.Mining;
import server.observers.Observer;

public interface MiningObserver extends Observer {

    /**
     * @return the time until the next mining action. resetting animations on success (if needed) is your responsibility
     */
    default int onOreMined(Player c, Mining mining, int objectId, int objectX, int objectY, int objectFace, int oreId, int levelReq, int xp){
        return -1;
    }

}
