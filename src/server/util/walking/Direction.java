package server.util.walking;

import server.model.npcs.Coordinate;

public class Direction {

    public static Direction from(DirectionDelta delta){
        DirectionDelta clipped = delta.clip();
        int dx = clipped.dx();
        int dy = clipped.dy();
        if (dx == 1 && dy == 0) {
            return Directions.EAST;
        } else if (dx == -1 && dy == 0) {
            return Directions.WEST;
        } else if (dx == 0 && dy == 1) {
            return Directions.NORTH;
        } else if (dx == 0 && dy == -1) {
            return Directions.SOUTH;
        } else if (dx == 1 && dy == 1) {
            return Directions.NORTHEAST;
        } else if (dx == -1 && dy == 1) {
            return Directions.NORTHWEST;
        } else if (dx == 1 && dy == -1) {
            return Directions.SOUTHEAST;
        } else if (dx == -1 && dy == -1) {
            return Directions.SOUTHWEST;
        } else {
            return Directions.NOSTEP;
        }
    }

    public static Direction from(Coordinate from, Coordinate to){
        return from(DirectionDelta.from(from, to));
    }

    public static Direction from(int fromX, int fromY, int toX, int toY){
        return from(DirectionDelta.from(fromX, fromY, toX, toY));
    }

    public final String text;
    public final int dx;
    public final int dy;
    public final int clip;
    public final int value; // for now, this is only confirmed compatible with forceMovement

    public DirectionDelta delta(){
        return new DirectionDelta(this.dx, this.dy);
    }

    public Direction opposite() {
        return opposite;
    }

    public boolean isDiagonal(){
        return dx != 0 && dy != 0;
    }

    public Direction[] decomposeDiagonal(){
        return cardinals;
    }

    public Direction[] perpendicular(){
        return perpendiculars;
    }

    @Override
    public String toString() {
        return "Direction{" +
            "text='" + text + '\'' +
            ", dx=" + dx +
            ", dy=" + dy +
            '}';
    }

    Direction(String name, int dx, int dy, int clip, int value){
        this.text = name;
        this.dx = dx;
        this.dy  = dy;
        this.clip = clip;
        this.value = value;
    }

    private Direction opposite;
    final void initializeOpposite(Direction opposite){
        this.opposite = this.opposite == null ? opposite : this.opposite;
    }

    private Direction[] cardinals;
    final void initializeCardinals(Direction... cardinals){
        this.cardinals = this.cardinals == null ? cardinals : this.cardinals;
    }

    private Direction[] perpendiculars;
    final void initializePerpendiculars(Direction... perpendiculars){
        this.perpendiculars = this.perpendiculars == null ? perpendiculars : this.perpendiculars;
    }

    // long term todos:
    // make all client systems use the value mapping from the value field
}
