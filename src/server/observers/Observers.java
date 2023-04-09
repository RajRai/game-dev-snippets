package server.observers;

import server.model.players.Player;
import server.observers.players.ActionObserver;
import server.observers.players.MiningObserver;
import server.util.TextUtils;

import java.util.ArrayList;

public class Observers {

    public static final ArrayList<ActionObserver> actionObservers = new ArrayList<>();
    public static final ArrayList<MiningObserver> miningObservers = new ArrayList<>();

    // todo: find a way to generify these methods, as well as debug messages
    public static boolean checkItemActionObservers(Player c, int itemId, int slot, int entry){
        for (ActionObserver ao : Observers.actionObservers){
            if (ao.onItemOption(c, itemId, slot, entry)){
                if (c.debug){
                    c.sendMessages(TextUtils.splitTextToFitChat("ActionObserver \"" + ao.getName() + "\" processed the item click"));
                }
                return true;
            }
        }
        return false;
    }

    public static boolean checkUseItemOnObject(Player c, int itemId, int obId, int obX, int obY, int obFace){
        for (ActionObserver ao : Observers.actionObservers){
            if (ao.onItemOnObject(c, itemId, obId, obX, obY, obFace)){
                if (c.debug){
                    c.sendMessages(TextUtils.splitTextToFitChat("ActionObserver \"" + ao.getName() + "\" processed the item on object"));
                }
                return true;
            }
        }
        return false;
    }

}
