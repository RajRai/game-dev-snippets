package server.model.npcs.instances.tombs_of_amascut_WIP.het;

import server.model.npcs.NPC;
import server.model.npcs.NPCConstants;
import server.model.npcs.NPCHandler;
import server.model.npcs.NpcId;
import server.model.npcs.instances.tombs_of_amascut_WIP.TombsInstance;
import server.model.npcs.instances.tombs_of_amascut_WIP.TombsRoom;
import server.model.players.Player;
import server.util.Misc;
import server.world.bounding.WorldArea;

public class AkkhaRoom extends TombsRoom {
    private NPC akkha;

    public AkkhaRoom(TombsInstance instance, WorldArea worldArea) {
        super(instance, worldArea);
    }

    @Override
    protected void setup(Player c) {
        // todo: scale stats
        akkha = NPCHandler.newNPC(NpcId.AKKHA_11787, 3680, 5408, instance.getHeightLevel()+1, 99, NPCConstants.getNpcSpawnInfo(NpcId.AKKHA_11787), instance);
    }

    @Override
    protected void teleportInside(Player c) {
        instance.teleportWithBlackFade(c, 3698, 5407 + Misc.random(1), 1);
    }

    @Override
    protected void start(Player c) {
        akkha.attackingPlayerIndex = c.playerIndex;
    }
}
