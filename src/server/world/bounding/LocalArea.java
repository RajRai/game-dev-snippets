package server.world.bounding;

import server.model.Entity;

public interface LocalArea {

    boolean contains(int x, int y, int z);

    default boolean contains(Entity e){
        return e != null && contains(e.absX, e.absY, e.heightLevel);
    }

}
