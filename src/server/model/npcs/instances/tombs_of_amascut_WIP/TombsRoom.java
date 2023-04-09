package server.model.npcs.instances.tombs_of_amascut_WIP;

import server.model.objects.ObjectId;
import server.model.players.Player;
import server.world.ObjectManager;
import server.world.bounding.LocalArea;
import server.world.bounding.WorldArea;

import java.util.HashSet;

public abstract class TombsRoom {

    public final TombsInstance instance;
    boolean isSetUp = false;
    protected final HashSet<Player> playerStarted = new HashSet<>();
    protected final LocalArea localArea;
    boolean isFinished = false;

    protected TombsRoom(TombsInstance instance, WorldArea worldArea) {
        this.instance = instance;
        this.localArea = worldArea.localize(instance.getHeightLevel());
    }

    // Entering and starting are not the same. Worth noting that playerStarted is not meant to be updated until after
    // the first player uses the crystal or passes the first barrier
    protected void enter(Player c){
        if (!isSetUp){
            instance.currentRoom = this;
            setup(c);
            isSetUp = true;
        }
        teleportInside(c);
    }

    protected final boolean isFinished(){
        return isFinished;
    }

    protected final void finish(){
        instance.currentRoom = null;
        isFinished = true;
        destroy();
    }

    /**
     * Called when the first player enters the lobby area (first teleportInside call)
     */
    protected abstract void setup(Player c);

    protected abstract void teleportInside(Player c);

    /**
     * Called when the first player actually enters the puzzle or boss fight.
     */
    protected abstract void start(Player c);

    protected void destroy(){
        ObjectManager.removeIf(object ->
            localArea.contains(object.getObjectX(), object.getObjectY(), object.getHeight()) &&
            object.getObjectId() == ObjectId.BARRIER_45135
        );
    }
}
