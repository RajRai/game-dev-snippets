package server.model.npcs;

import server.model.npcs.instances.Instance;
import server.model.players.Player;

public class NPCBuilder {

    public int npcType;
    public Player c = null;
    public int x;
    public int y;
    public int walkingType = 0;
    public NpcSpawnInfo spawnInfo = null;
    public boolean attackPlayer = false;
    public boolean headIcon = false;
    public int secondHeadIcon = -1;
    public boolean isFamiliar = false;
    public int height = 0;
    public Instance instance = null;

    public NPCBuilder(int npcType){
        this.npcType = npcType;
    }

    public NPC build(){
        return NPCHandler.spawnNpc(this);
    }

    public NPCBuilder player(Player c){
        this.c = c;
        return this;
    }

    public NPCBuilder x(int x){
        this.x= x;
        return this;
    }

    public NPCBuilder y(int y){
        this.y = y;
        return this;
    }

    public NPCBuilder walkingType(int walkingType){
        this.walkingType = walkingType;
        return this;
    }

    public NPCBuilder spawnInfo(NpcSpawnInfo spawnInfo){
        this.spawnInfo = spawnInfo;
        return this;
    }

    public NPCBuilder attackPlayer(boolean attackPlayer){
        this.attackPlayer = attackPlayer;
        return this;
    }

    public NPCBuilder headIcon(boolean headIcon){
        this.headIcon = headIcon;
        return this;
    }

    public NPCBuilder secondHeadIcon(int secondHeadIcon){
        this.secondHeadIcon = secondHeadIcon;
        return this;
    }

    public NPCBuilder isFamiliar(boolean isFamiliar){
        this.isFamiliar = isFamiliar;
        return this;
    }

    public NPCBuilder height(int height){
        this.height = height;
        return this;
    }

    public NPCBuilder instance(Instance instance){
        this.instance = instance;
        return this;
    }

}
