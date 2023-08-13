package server.util.walking;

import server.model.npcs.Coordinate;
import server.util.Misc;

public record DirectionDelta(int dx, int dy) {

    public static DirectionDelta from(Coordinate from, Coordinate to) {
        return new DirectionDelta(to.getX() - from.getX(), to.getY() - from.getY());
    }

    public static DirectionDelta from(int fromX, int fromY, int toX, int toY){
        return new DirectionDelta(toX-fromX, toY-fromY);
    }

    public DirectionDelta scale(int factor) {
        return scale(factor, factor);
    }

    public DirectionDelta scale(int xFactor, int yFactor) {
        return new DirectionDelta(dx * xFactor, dy * yFactor);
    }

    public DirectionDelta scale(DirectionDelta other) {
        return scale(other.dx, other.dy);
    }

    public DirectionDelta plus(DirectionDelta other) {
        return new DirectionDelta(dx + other.dx, dy + other.dy);
    }

    public DirectionDelta clip() {
        return new DirectionDelta(Misc.clamp(dx, -1, 1), Misc.clamp(dy, -1, 1));
    }

    /**
     * @return The number of steps to travel the delta, assuming no blockages
     */
    public int gridDistance() {
        return Math.max(Math.abs(dx), Math.abs(dy));
    }

    public double visualDistanceHeuristic() {
        return this.dx * this.dx + this.dy * this.dy;
    }

    public double visualDistance() {
        return Math.sqrt(visualDistanceHeuristic());
    }

    public DirectionDelta rotate(int n) {
        int rotations = (n % 4 + 4) % 4; // Wrap rotation value within 0-3 range

        int rotatedDx;
        int rotatedDy;

        switch (rotations) {
            case Directions.ROTATE_0_DEGREES -> {
                rotatedDx = dx;
                rotatedDy = dy;
            }
            case Directions.ROTATE_90_DEGREES -> {
                rotatedDx = dy;
                rotatedDy = -dx;
            }
            case Directions.ROTATE_180_DEGREES -> {
                rotatedDx = -dx;
                rotatedDy = -dy;
            }
            case Directions.ROTATE_270_DEGREES -> {
                rotatedDx = -dy;
                rotatedDy = dx;
            }
            default -> throw new IllegalArgumentException("Invalid rotation value");
        }

        return new DirectionDelta(rotatedDx, rotatedDy);
    }
}
