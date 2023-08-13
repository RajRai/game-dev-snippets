package server.world.bounding;

import server.model.players.Player;

public interface LocalArea {

    boolean contains(int x, int y, int z);

    default boolean contains(Locatable l){
        Player p = null;
        if (l instanceof Player) p = (Player) l;
        return l != null && (contains(l.getX(), l.getY(), l.getZ()) || (p != null && contains(p.teleportToX, p.teleportToY, p.teleHeight) || (p != null && contains(p.teleX, p.teleY, p.teleHeight))));
    }

}
