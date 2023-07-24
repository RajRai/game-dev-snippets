package server.util.walking;

import server.model.npcs.Coordinate;

import java.util.Arrays;
import java.util.List;

public class Directions {

    public Direction getDirection(int value){
        return DIRECTIONS.get(value);
    }

    /*
     * The values of these variables should map to the corresponding indices for the DELTAS and CLIPS lists.
     */
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

    /*
     * For object face values.
     * Clockwise rotations, starting from the object base rotation (::object *id*)
     */
    public static final int ROTATE_0_DEGREES = 0;
    public static final int ROTATE_90_DEGREES = 1;
    public static final int ROTATE_180_DEGREES = 2;
    public static final int ROTATE_270_DEGREES = 3;

    public static Direction[] diagonalToStandardPair(Direction direction){
        if (NORTHEAST.equals(direction)) {
            return new Direction[]{EAST, NORTH};
        } else if (NORTHWEST.equals(direction)) {
            return new Direction[]{WEST, NORTH};
        } else if (SOUTHWEST.equals(direction)) {
            return new Direction[]{WEST, SOUTH};
        } else if (SOUTHEAST.equals(direction)) {
            return new Direction[]{EAST, SOUTH};
        }
        if (direction != null) {
            return new Direction[]{direction, direction};
        } else {
            return new Direction[]{NOSTEP, NOSTEP};
        }
    }

    public static boolean isDiagonalDirection(Direction direction){
        return direction.dx != 0 && direction.dy != 0;
    }

    public static Direction opposite(Direction direction){
        if (EAST.equals(direction)) {
            return WEST;
        } else if (NORTH.equals(direction)) {
            return SOUTH;
        } else if (WEST.equals(direction)) {
            return EAST;
        } else if (SOUTH.equals(direction)) {
            return NORTH;
        } else if (NORTHEAST.equals(direction)) {
            return SOUTHWEST;
        } else if (NORTHWEST.equals(direction)) {
            return SOUTHEAST;
        } else if (SOUTHWEST.equals(direction)) {
            return NORTHEAST;
        } else if (SOUTHEAST.equals(direction)) {
            return NORTHWEST;
        }
        return NOSTEP;
    }

    // todo: figure out which normal is inner and outer. i think because our direction rotation goes counter-clockwise, the clockwise normal is "outer"
    // todo: once that's verified, make sure the outer normal always comes first in the array
    public static Direction[] perpendicular(Direction direction){
        if (EAST.equals(direction) || WEST.equals(direction)) {
            return new Direction[]{NORTH, SOUTH};
        } else if (NORTH.equals(direction) || SOUTH.equals(direction)) {
            return new Direction[]{EAST, WEST};
        } else if (NORTHEAST.equals(direction) || SOUTHWEST.equals(direction)) {
            return new Direction[]{NORTHWEST, SOUTHEAST};
        } else if (NORTHWEST.equals(direction) || SOUTHEAST.equals(direction)) {
            return new Direction[]{NORTHEAST, SOUTHWEST};
        }
        return new Direction[]{NOSTEP, NOSTEP};
    }

    public static DirectionDelta getClippedDelta(Coordinate from, Coordinate to){
        return getClippedDelta(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static DirectionDelta getClippedDelta(int fromX, int fromY, int toX, int toY){
        int d1 = toX - fromX;
        int d2 = toY - fromY;
        return new DirectionDelta(
            Math.max(-1, Math.min(1, d1)),
            Math.max(-1, Math.min(1, d2))
        );
    }

    public static Direction deltaToDirection(DirectionDelta delta){
        for (Direction dir : DIRECTIONS){
            if (dir.dx == delta.dx() && dir.dy == delta.dy()){
                return dir;
            }
        }
        return NOSTEP;
    }

    public static Direction getDirection(Coordinate from, Coordinate to){
        return getDirection(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static Direction getDirection(int fromX, int fromY, int toX, int toY){
        return deltaToDirection(getClippedDelta(fromX, fromY, toX, toY));
    }
}
