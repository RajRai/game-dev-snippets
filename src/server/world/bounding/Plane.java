package server.world.bounding;

public class Plane implements LocalArea {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int height;

    public Plane(int x1, int y1, int x2, int y2, int height){
        this.xMin = Math.min(x1, x2);
        this.xMax = Math.max(x1, x2);
        this.yMin = Math.min(y1, y2);
        this.yMax = Math.max(y1, y2);
        this.height = height;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax && z == this.height;
    }
}
