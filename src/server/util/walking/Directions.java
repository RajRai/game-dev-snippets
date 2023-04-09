package server.util.walking;

import server.model.npcs.Coordinate;

import java.util.Arrays;
import java.util.List;

public class Directions {

    /*
     * The values of these variables should map to the corresponding indices for the DELTAS and CLIPS lists.
     */
    public static final int EAST = 0;
    public static final int NORTH = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;
    public static final int NORTHEAST = 4;
    public static final int NORTHWEST = 5;
    public static final int SOUTHWEST = 6;
    public static final int SOUTHEAST = 7;
    public static final int NOSTEP = 8;

    public static final List<int[]> DELTAS = Arrays.asList(new int[][] { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 }, { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 }, { 0, 0 } });

    public static final List<Integer> CARDINAL_DIRECTIONS = Arrays.asList(EAST, NORTH, WEST, SOUTH);
    public static final List<Integer> DIAGONAL_DIRECTIONS = Arrays.asList(NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST);
    public static final List<Integer> ALL_DIRECTIONS = Arrays.asList(EAST, NORTH, WEST, SOUTH, NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST);

    /*
     * For object face values.
     * Clockwise rotations, starting from the object base rotation (::object *id*)
     */
    public static final int ROTATE_0_DEGREES = 0;
    public static final int ROTATE_90_DEGREES = 1;
    public static final int ROTATE_180_DEGREES = 2;
    public static final int ROTATE_270_DEGREES = 3;

    public static int[] diagonalToStandardPair(int direction){
        switch (direction){
            case NORTHEAST:
                return new int[]{ EAST, NORTH };
            case NORTHWEST:
                return new int[]{ WEST, NORTH };
            case SOUTHWEST:
                return new int[]{ WEST, SOUTH };
            case SOUTHEAST:
                return new int[]{ EAST, SOUTH };
            default:
                if (direction >= 0 && direction < DELTAS.size()){
                    return DELTAS.get(direction);
                } else {
                    return new int[]{ NOSTEP, NOSTEP };
                }
        }
    }

    public static boolean isDiagonalDirection(int direction){
        return direction >= NORTHEAST && direction <= SOUTHEAST;
    }

    public static int opposite(int direction){
        switch (direction){
            case EAST:
                return WEST;
            case NORTH:
                return SOUTH;
            case WEST:
                return EAST;
            case SOUTH:
                return NORTH;
            case NORTHEAST:
                return SOUTHWEST;
            case NORTHWEST:
                return SOUTHEAST;
            case SOUTHWEST:
                return NORTHEAST;
            case SOUTHEAST:
                return NORTHWEST;
            default:
                return NOSTEP;
        }
    }

    // todo: figure out which normal is inner and outer. i think because our direction rotation goes counter-clockwise, the clockwise normal is "outer"
    // todo: once that's verified, make sure the outer normal always comes first in the array
    public static int[] perpendicular(int direction){
        switch (direction){
            case EAST:
            case WEST:
                return new int[]{NORTH, SOUTH};
            case NORTH:
            case SOUTH:
                return new int[]{EAST, WEST};
            case NORTHEAST:
            case SOUTHWEST:
                return new int[]{NORTHWEST, SOUTHEAST};
            case NORTHWEST:
            case SOUTHEAST:
                return new int[]{NORTHEAST, SOUTHWEST};
            default:
                return new int[]{NOSTEP, NOSTEP};
        }
    }

    public static int[] getClippedDelta(Coordinate from, Coordinate to){
        return getClippedDelta(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static int[] getClippedDelta(int fromX, int fromY, int toX, int toY){
        int d1 = toX - fromX;
        int d2 = toY - fromY;
        return new int[]{
            Math.max(-1, Math.min(1, d1)),
            Math.max(-1, Math.min(1, d2)),
        };
    }

    public static int deltaToDirection(int[] delta){
        for (int direction = 0; direction < DELTAS.size(); direction++){
            int[] value = DELTAS.get(direction);
            if (value[0] == delta[0] && value[1] == delta[1]){
                return direction;
            }
        }
        return NOSTEP;
    }

    public static int getDirection(Coordinate from, Coordinate to){
        return getDirection(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static int getDirection(int fromX, int fromY, int toX, int toY){
        return deltaToDirection(getClippedDelta(fromX, fromY, toX, toY));
    }
}
