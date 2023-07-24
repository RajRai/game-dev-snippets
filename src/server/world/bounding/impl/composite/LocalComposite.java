package server.world.bounding.impl.composite;

import server.model.Entity;
import server.world.bounding.LocalArea;

public class LocalComposite implements LocalArea {

    private final LocalArea[] children;

    public LocalComposite(LocalArea[] children){
        this.children = children;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        for (LocalArea c : children){
            if (c.contains(x, y, z))
                return true;
        }
        return false;
    }

    @Override
    public boolean contains(Entity e) {
        if (e == null){
            return false;
        }
        for (LocalArea c : children){
            if (c.contains(e))
                return true;
        }
        return false;
    }
}
