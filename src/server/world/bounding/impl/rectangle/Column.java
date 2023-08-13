package server.world.bounding.impl.rectangle;

import server.model.npcs.Coordinate;
import server.util.walking.DirectionDelta;
import server.util.walking.Directions;
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

    protected Coordinate makeCoordinate(int x, int y) {
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

    public Coordinate rotateCoordinate(Coordinate originalCoord, int rotation) {
        rotation = (rotation % 4 + 4) % 4;
        Coordinate center = new Coordinate((xMin + xMax) / 2, (yMin + yMax) / 2);
        DirectionDelta delta = DirectionDelta.from(center, originalCoord);
        DirectionDelta centerShift = new DirectionDelta(0, 0);
        // Rotate the center for even grid boxes
        if ((xMax - xMin) % 2 == 0 && (yMax - yMin) % 2 == 0) {
            if (rotation == Directions.ROTATE_90_DEGREES) {
                centerShift = new DirectionDelta(0, -1);
            } else if (rotation == Directions.ROTATE_180_DEGREES) {
                centerShift = new DirectionDelta(-1, -1);
            } else if (rotation == Directions.ROTATE_270_DEGREES) {
                centerShift = new DirectionDelta(-1, 0);
            }
        }

        return center.plus(centerShift).plus(delta.rotate(rotation));
    }


}
