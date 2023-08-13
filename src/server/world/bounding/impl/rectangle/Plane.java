package server.world.bounding.impl.rectangle;

import server.model.npcs.Coordinate;
import server.world.bounding.LocalArea;
import server.world.bounding.Locatable;

public class Plane extends Column implements LocalArea {

    private final int height;

    public Plane(int x1, int y1, int x2, int y2, int height){
        super(x1, y1, x2, y2);
        this.height = height;
    }

    public Plane(int centerX, int centerY, int pad, int height){
        super(centerX, centerY, pad);
        this.height = height;
    }

    @Override
    protected Coordinate makeCoordinate(int x, int y) {
        return new Coordinate(x, y, height);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return super.contains(x, y) && z == this.height;
    }

    @Override
    public boolean contains(Locatable l) {
        return LocalArea.super.contains(l);
    }
}
