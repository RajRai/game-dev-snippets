package server.world.bounding;

import server.model.Entity;

public class WorldComposite implements WorldArea {

    private final WorldArea[] children;

    public WorldComposite(WorldArea... children){
        this.children = children;
    }

    @Override
    public boolean contains(int x, int y) {
        for (WorldArea c : children){
            if (c.contains(x, y))
                return true;
        }
        return false;
    }

    @Override
    public boolean contains(Entity e) {
        for (WorldArea c : children){
            if (c.contains(e))
                return true;
        }
        return false;
    }

    @Override
    public LocalArea localize(int baseHeight) {
        LocalArea[] children = new LocalArea[this.children.length];
        for (int i = 0; i < this.children.length; i++){
            children[i] = this.children[i].localize(baseHeight);
        }
        return new LocalComposite(children);
    }
}
