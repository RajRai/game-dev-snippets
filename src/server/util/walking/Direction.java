package server.util.walking;

public class Direction {

    public final String text;
    public final int dx;
    public final int dy;
    public final int clip;
    public final int value; // for now, this is only confirmed compatible with forceMovement

    public Direction(String name, int dx, int dy, int clip, int value){
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

    // If the clip &'d with the map/shootable data for a tile is not equal to 0, the tile cannot be entered from that direction.
    // Essentially, each bit in the last byte of the map data represents if the tile is blocked when entering from a direction.
    // By &'ing it with the clip values, the result will be 0 when there are no blockages entering from the checked directions, or non-zero otherwise
    // To check if a square can be entered, you need to check the map data against the & clip in the direction for the tile you're entering, and
    // the & clip in the opposite direction for the tile you're leaving
    // These might or might not be wrong. It's more of a concept/plan currently

    // long term todos:
    // make all client systems use the value mapping from the value field
    // validate the clip values
}
