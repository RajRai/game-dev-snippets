package server.world.bounding.impl.rectangle;

import server.model.Entity;
import server.model.npcs.Coordinate;
import server.world.bounding.LocalArea;

public class Plane extends Column implements LocalArea {

    private final int height;

    public Plane(int x1, int y1, int x2, int y2, int height){
        super(x1, y1, x2, y2);
        this.height = height;
    }

    public Plane(int centerX, int centerY, int pad, int height){
        this(centerX - pad, centerY - pad, centerX + pad, centerY + pad, height);
    }

    @Override
    public Coordinate makeCoordinate(int x, int y) {
        return new Coordinate(x, y, height);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax && z == this.height;
    }

    @Override
    public boolean contains(Entity e) {
        return LocalArea.super.contains(e);
    }
}
