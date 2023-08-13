package server.world.bounding;

import server.model.players.Player;

public interface WorldArea {

    boolean contains(int x, int y);

    LocalArea localize(int baseHeight);

    default boolean contains(Locatable l){
        Player p = null;
        if (l instanceof Player) p = (Player) l;
        return l != null && (contains(l.getX(), l.getY()) || (p != null && contains(p.teleportToX, p.teleportToY) || (p != null && contains(p.teleX, p.teleY))));
    }

}
