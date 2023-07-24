package server.world.bounding.impl.rectangle;

import server.model.npcs.Coordinate;
import server.world.bounding.LocalArea;
import server.world.bounding.WorldArea;

import java.util.Iterator;

public class Column implements Iterable<Coordinate>, WorldArea {

    protected final int xMin;
    protected final int xMax;
    protected final int yMin;
    protected final int yMax;

    public Column(int x1, int y1, int x2, int y2){
        this.xMin = Math.min(x1, x2);
        this.xMax = Math.max(x1, x2);
        this.yMin = Math.min(y1, y2);
        this.yMax = Math.max(y1, y2);
    }

    public Column(int centerX, int centerY, int pad){
        this(centerX-pad, centerY-pad, centerX+pad, centerY+pad);
    }

    @Override
    public boolean contains(int x, int y){
        return x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax;
    }

    @Override
    public LocalArea localize(int baseHeight) {
        return new Plane(xMin, yMin, xMax, yMax, baseHeight);
    }

    public int[][] asXYArray(){
        int[][] out = new int[(xMax-xMin)*(yMax-yMin)][2];
        int i = 0;
        for (Coordinate c : this){
            out[i] = c.asXYArray();
        }
        return out;
    }

    @Override
    public Iterator<Coordinate> iterator() {
        return new RectangleIterator();
    }

    public Coordinate makeCoordinate(int x, int y) {
        return new Coordinate(x, y);
    }

    public class RectangleIterator implements Iterator<Coordinate> {
        int currentX = xMin;
        int currentY = yMin;

        @Override
        public boolean hasNext() {
            return currentY <= yMax && currentX <= xMax;
        }

        @Override
        public Coordinate next() {
            Coordinate nextCoordinate = makeCoordinate(currentX, currentY);
            if (currentX < xMax) {
                currentX++;
            } else {
                currentX = xMin;
                currentY++;
            }
            return nextCoordinate;
        }
    }

}
