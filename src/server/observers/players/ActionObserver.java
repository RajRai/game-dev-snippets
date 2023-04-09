package server.observers.players;
import server.model.npcs.NPC;
import server.model.objects.WorldObject;
import server.model.players.Player;
import server.observers.Observer;

public interface ActionObserver extends Observer {

    default boolean onObjectOption(Player c, WorldObject object, int obId, int obX, int obY, int obFace, int entry){ return false; }

    default boolean onNPCOption(Player c, NPC npc, int npcIndex, int entry){ return false; }

    default boolean onItemOption(Player c, int itemId, int slot, int entry){ return false; }

    default boolean onItemOnObject(Player c, int itemId, int obId, int obX, int obY, int obFace){ return false; }

}
