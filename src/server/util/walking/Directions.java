package server.util.walking;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Directions {

    public static final Direction NORTH = new Direction("N", 0, 1, 0x1280102, 0);
    public static final Direction EAST = new Direction("E", 1, 0, 0x1280108, 1);
    public static final Direction SOUTH = new Direction("S", 0, -1, 0x1280120, 2);
    public static final Direction WEST =  new Direction("W", -1, 0, 0x1280180, 3);
    public static final Direction NORTHEAST = new Direction("NE", 1, 1, 0x128010e, 4);
    public static final Direction SOUTHEAST = new Direction("SE", 1, -1, 0x1280183, 5);
    public static final Direction SOUTHWEST = new Direction("SW", -1, -1, 0x12801e0, 6);
    public static final Direction NORTHWEST = new Direction("NW", -1, 1, 0x1280138, 7);
    public static final Direction NOSTEP = new Direction("NOSTEP", 0, 0, 0x1280100, 8);

    public static final List<Direction> DIRECTIONS = Arrays.asList(EAST, NORTH, WEST, SOUTH, NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST);
    public static final List<Direction> CARDINAL_DIRECTIONS = Arrays.asList(EAST, NORTH, WEST, SOUTH);
    public static final List<Direction> DIAGONAL_DIRECTIONS = Arrays.asList(NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST);

    // Delta to Direction map
    protected static final Map<DirectionDelta, Direction> DELTA_TO_DIRECTION = new HashMap<>();
    static {
        for (Direction direction : DIRECTIONS) {
            // Initialize the delta to direction mappings
            DELTA_TO_DIRECTION.put(direction.delta(), direction);
        }
    }

    public static final int ROTATE_0_DEGREES = 0;
    public static final int ROTATE_90_DEGREES = 1;
    public static final int ROTATE_180_DEGREES = 2;
    public static final int ROTATE_270_DEGREES = 3;

    public static final int ROTATE_0_DEGREES_COUNTER_CLOCKWISE = ROTATE_0_DEGREES;
    public static final int ROTATE_90_DEGREES_COUNTER_CLOCKWISE = ROTATE_270_DEGREES;
    public static final int ROTATE_180_DEGREES_COUNTER_CLOCKWISE = ROTATE_180_DEGREES;
    public static final int ROTATE_270_DEGREES_COUNTER_CLOCKWISE = ROTATE_90_DEGREES;

    public static int invertRotation(int rotation){
        return switch (rotation) {
            case ROTATE_0_DEGREES -> ROTATE_0_DEGREES_COUNTER_CLOCKWISE;
            case ROTATE_90_DEGREES -> ROTATE_90_DEGREES_COUNTER_CLOCKWISE;
            case ROTATE_180_DEGREES -> ROTATE_180_DEGREES_COUNTER_CLOCKWISE;
            case ROTATE_270_DEGREES -> ROTATE_270_DEGREES_COUNTER_CLOCKWISE;
            default -> throw new IllegalArgumentException("Invalid rotation value: " + rotation);
        };
    }
}
