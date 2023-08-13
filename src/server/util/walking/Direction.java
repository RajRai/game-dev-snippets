package server.util.walking;

import server.model.npcs.Coordinate;

public class Direction {

    public static Direction from(DirectionDelta delta){
        return Directions.DELTA_TO_DIRECTION.getOrDefault(delta.clip(), Directions.NOSTEP);
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

    protected Direction(String name, int dx, int dy, int clip, int value){
        this.text = name;
        this.dx = dx;
        this.dy  = dy;
        this.clip = clip;
        this.value = value;
    }

    public DirectionDelta delta(){
        return new DirectionDelta(this.dx, this.dy);
    }

    @Override
    public String toString() {
        return "Direction{" +
            "text='" + text + '\'' +
            ", dx=" + dx +
            ", dy=" + dy +
            '}';
    }

    public Direction opposite() {
        return from(delta().scale(-1));
    }

    public boolean isDiagonal(){
        return dx != 0 && dy != 0;
    }

    public Direction[] decomposeDiagonal(){
        return new Direction[]{
            from(new DirectionDelta(dx, 0)),
            from(new DirectionDelta(0, dy))
        };
    }

    public Direction[] perpendicular(){
        return new Direction[]{
            from(delta().rotate(Directions.ROTATE_90_DEGREES)),
            from(delta().rotate(Directions.ROTATE_270_DEGREES))
        };
    }

    // long term todos:
    // make all client systems use the value mapping from the value field
}
