package server.model.npcs.instances.tombs_of_amascut_WIP;

import server.model.npcs.NPC;
import server.model.npcs.NPCConstants;
import server.model.npcs.NPCHandler;
import server.model.npcs.NpcSpawnInfo;

public class TombsNPC extends NPC {

    protected TombsNPC(int slot, int npcType, int absX_, int absY_, int heightLevel, NpcSpawnInfo spawnInfo, int walkingType){
        super(slot, npcType, absX_, absY_, heightLevel);
        NPCHandler.addNPC(slot, this, spawnInfo, walkingType);
    }

    protected TombsNPC(int slot, int npcType, int absX_, int absY_, int heightLevel, int walkingType){
        this(slot, npcType, absX_, absY_, heightLevel, NPCConstants.getNpcSpawnInfo(npcType), walkingType);
    }
}
