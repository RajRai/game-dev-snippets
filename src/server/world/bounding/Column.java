package server.world.bounding;

public class Column implements WorldArea {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;

    public Column(int x1, int y1, int x2, int y2){
        this.xMin = Math.min(x1, x2);
        this.xMax = Math.max(x1, x2);
        this.yMin = Math.min(y1, y2);
        this.yMax = Math.max(y1, y2);
    }

    public boolean contains(int x, int y){
        return x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax;
    }

    @Override
    public LocalArea localize(int baseHeight) {
        return new Plane(xMin, yMin, xMax, yMax, baseHeight);
    }
}
