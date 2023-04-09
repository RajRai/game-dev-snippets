package server.model.npcs.instances;

import server.model.players.Player;

public interface InstanceBuilder {

    Instance build(Player owner, InstanceType type, int height);

}
