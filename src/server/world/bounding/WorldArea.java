package server.world.bounding;

import server.model.Entity;

public interface WorldArea {

    boolean contains(int x, int y);

    LocalArea localize(int baseHeight);

    default boolean contains(Entity e){
        return e != null && contains(e.absX, e.absY);
    }

}
