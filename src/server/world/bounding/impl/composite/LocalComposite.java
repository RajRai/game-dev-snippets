package server.world.bounding.impl.composite;

import server.world.bounding.LocalArea;
import server.world.bounding.Locatable;

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
    public boolean contains(Locatable l) {
        if (l == null){
            return false;
        }
        for (LocalArea c : children){
            if (c.contains(l))
                return true;
        }
        return false;
    }
}
