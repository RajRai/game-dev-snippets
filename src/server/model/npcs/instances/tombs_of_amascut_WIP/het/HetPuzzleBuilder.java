package server.model.npcs.instances.tombs_of_amascut_WIP.het;

import server.model.npcs.Coordinate;
import server.model.objects.Lightbeam;
import server.model.objects.ObjectId;
import server.model.objects.WorldObject;
import server.util.Misc;
import server.util.walking.Directions;
import server.world.bounding.Column;
import server.world.bounding.WorldComposite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class HetPuzzleBuilder {

    /*
     * End pieces are for the eastern end of a north/south facing wall (direction = east/west)
     * Rotate accordingly
     */
    private static final int UNBREAKABLE_MID = ObjectId.BARRIER_45458;
    private static final int UNBREAKABLE_END = ObjectId.BARRIER_45460;
    private static final int BREAKABLE_MID = ObjectId.BARRIER_45462;
    private static final int BREAKABLE_END = ObjectId.BARRIER_45464;

    // todo: add better geometry to better define the valid lightbeam area
    private static final Column area = new Column(3672, 5272, 3688, 5288);
    private static final WorldComposite exclusion = new WorldComposite(
        // 9x3 area for statues + seal
        new Column(3676, 5279, 3684, 5281),
        // 3x3 columns in the corners of the area. Improves room geometry and guarantees pathing
        new Column(3688, 5286, 3686, 5288),
        new Column(3688, 5274, 3686, 5272),
        new Column(3672, 5274, 3674, 5272),
        new Column(3672, 5286, 3674, 5288)
    );

    public static HetPuzzleObjects build(int height, int players){
        HetPuzzleLayout layout = makeLayout(0);
        int tries = 0;
        while(tries++ < 100 && (layout.breakableBarriers.size() == 0 || layout.unbreakableBarriers.size() == 0)){
            layout = makeLayout(players);
        }
        return getPuzzle(height, layout);
    }

    public static HetPuzzleObjects getPuzzle(int height, HetPuzzleLayout layout){
        HetPuzzleObjects objects = new HetPuzzleObjects();
        layout.mirrors.forEach(mirror -> {
            objects.mirrors.add(placeMirror(mirror.id, mirror.x, mirror.y, height, mirror.face));
        });
        placeBarriers(objects.breakableBarriers, layout.breakableBarriers, BREAKABLE_MID, BREAKABLE_END, height);
        placeBarriers(objects.unbreakableBarriers, layout.unbreakableBarriers, UNBREAKABLE_MID, UNBREAKABLE_END, height);
        return objects;
    }
    
    private static void placeBarriers(ArrayList<WorldObject> into, ArrayList<PathCoordinate> locations, int midId, int endId, int height){
        locations.forEach(c -> {
            int[] delta = Directions.DELTAS.get(c.direction);
            boolean topNeighbor = locations.stream().anyMatch(coord -> coord.getX() == c.getX() + delta[0] && coord.getY() == c.getY() + delta[1]);
            boolean bottomNeighbor = locations.stream().anyMatch(coord -> coord.getX() == c.getX() - delta[0] && coord.getY() == c.getY() - delta[1]);
            if (topNeighbor && bottomNeighbor) {
                // Just a random middle barrier
                into.add(placeBarrier(midId, c.getX(), c.getY(), height, barrierDirectionToFace(c.direction)));
            } else if (bottomNeighbor){
                // We're at the top end - just rotate to account for the face
                into.add(placeBarrier(endId, c.getX(), c.getY(), height, barrierDirectionToFace(c.direction)));
            } else if (topNeighbor){
                // Bottom end, so rotate 180
                into.add(placeBarrier(endId, c.getX(), c.getY(), height, barrierDirectionToFace(Directions.opposite(c.direction))));
            }
        });
    }

    private static int barrierDirectionToFace(int direction){
        switch (direction){
            case Directions.EAST:
                return Directions.ROTATE_0_DEGREES;
            case Directions.SOUTH:
                return Directions.ROTATE_90_DEGREES;
            case Directions.WEST:
                return Directions.ROTATE_180_DEGREES;
            case Directions.NORTH:
                return Directions.ROTATE_270_DEGREES;
            default:
                return 0;
        }
    }

    public static HetPuzzleLayout makeLayout(int players){
        // First, let's make a random light beam path, so we can make sure at least one path works.
        ArrayList<Coordinate> anchors = generateLightbeamAnchors();
        return getLayout(anchors, players);
    }

    private static ArrayList<Coordinate> generateLightbeamAnchors(){
        // We start from a random tile near the shielded statue, working backwards to the start.
        // This will require either 2, 3, or 4 mirrors, randomly.
        // For the case of 2, this will be accomplished by pre-placing a mirror in the path.
        // 4 turns happens if the path is meant to collide with the eastern side of the shielded statue (x == 3685)
        int[] xStart = new int[]{
            3682,
            3683,
            3684,
            3685,
            3685,
            3685,
            3684,
            3683,
            3682
        };
        int[] yStart = new int[]{
            5278,
            5278,
            5278,
            5279,
            5280,
            5281,
            5282,
            5282,
            5282
        };

        int startIndex = Misc.random(xStart.length-1);
        int x = xStart[startIndex];
        int y = yStart[startIndex];

        int numTurns = x == 3685 ? 4 : 3;
        ArrayList<Coordinate> points = new ArrayList<>();
        points.add(new Coordinate(x, y));

        // For paths leaving to the East, there are 4 turns. Conditionally add the first.
        if (numTurns == 4){
            Coordinate newCoords = continueLine(x, y, Directions.EAST);
            points.add(newCoords);
            x = newCoords.getX();
            y = newCoords.getY();

            // Pick north or south randomly.
            int direction = Misc.random(1) == 0 ? Directions.NORTH : Directions.SOUTH;
            // Move in that direction until we're aligned with the north/south shielded statue exit tiles
            // Once we're aligned, we'll just use their solution for the rest of the path
            while (y != 5278 && y != 5282){
                int[] delta = Directions.DELTAS.get(direction);
                x += delta[0];
                y += delta[1];
            }
        }

        // If we're South of the statue, we're moving South
        int direction = y == 5278 ? Directions.SOUTH : Directions.NORTH;
        // We're now leaving the shielded statue in a known direction. We need to turn west after some random tiles.
        Coordinate newCoords = continueLine(x, y, direction);
        points.add(newCoords);
        x = newCoords.getX();
        y = newCoords.getY();

        // So now let's head west. We need to go until at least x == 3675. From there, we can continueLine randomly
        while (x != 3675){
            int[] delta = Directions.DELTAS.get(Directions.WEST);
            x += delta[0];
            y += delta[1];
        }

        // Continue the line randomly west
        newCoords = continueLine(x, y, Directions.WEST);
        points.add(newCoords);
        x = newCoords.getX();
        y = newCoords.getY();

        // Go south or north until we're aligned with the ligthbeam start row
        direction = y > 5280 ? Directions.SOUTH : Directions.NORTH;
        while (y != 5280){
            int[] delta = Directions.DELTAS.get(direction);
            x += delta[0];
            y += delta[1];
        }

        // Whatever point we align on is our last (or first, since we started from the end) turn point
        points.add(new Coordinate(x, y));

        // Add the start tile. We add the start and end tiles so that deriving the implied mirror face is easy.
        points.add(new Coordinate(3676, 5280));

        // Reverse it, since we went from end to beginning
        Collections.reverse(points);
        return points;
    }

    private static Coordinate continueLine(int startX, int startY, int direction){
        int x = startX;
        int y = startY;
        int maxLength = 0;
        int[] delta = Directions.DELTAS.get(direction);
        while (area.contains(x, y) && !exclusion.contains(x, y)){
            x += delta[0];
            y += delta[1];
            maxLength++;
        }
        int length = Misc.random(Math.max(maxLength-1, 0));
        return new Coordinate(startX + delta[0] * length, startY + delta[1] * length);
    }

    private static ArrayList<MirrorConfiguration> pointsToMirrorConfigs(ArrayList<Coordinate> points){
        ArrayList<MirrorConfiguration> out = new ArrayList<>();
        IntStream.range(1, points.size()-1).forEach((i) -> {
            Coordinate prev = points.get(i-1);
            Coordinate curr = points.get(i);
            Coordinate next = points.get(i+1);

            int[] d1 = Directions.getClippedDelta(curr, prev);
            int[] d2 = Directions.getClippedDelta(curr, next);
            int[] delta = new int[]{
                d1[0] + d2[0],
                d1[1] + d2[1],
            };
            int direction = Directions.deltaToDirection(delta);
            out.add(new MirrorConfiguration(curr.getX(), curr.getY(), Lightbeam.directionToMirror(direction), ObjectId.MIRROR_45456));
        });
        return out;
    }
    
    private static ArrayList<PathCoordinate> getPath(ArrayList<Coordinate> points){
        ArrayList<PathCoordinate> tiles = new ArrayList<>();
        IntStream.range(1, points.size()).forEach(index -> {
            Coordinate current = points.get(index-1);
            Coordinate next = points.get(index);
            int[] delta = Directions.getClippedDelta(current, next);
            int x = current.getX();
            int y = current.getY();
            while (x != next.getX() || y != next.getY()){
                x += delta[0];
                y += delta[1];
                tiles.add(new PathCoordinate(x, y, Directions.deltaToDirection(delta)));
            }
        });
        return tiles;
    }

    private static HetPuzzleLayout getLayout(ArrayList<Coordinate> anchors, int players){
        // Setup
        ArrayList<MirrorConfiguration> solution = pointsToMirrorConfigs(anchors);
        ArrayList<PathCoordinate> beamPathTiles = getPath(anchors);
        ArrayList<Coordinate> illegalTiles = new ArrayList<>();

        // Add the corners of the room to the blocked tiles to guarantee pathing
        illegalTiles.add(new Coordinate(area.xMin, area.yMin));
        illegalTiles.add(new Coordinate(area.xMin, area.yMax));
        illegalTiles.add(new Coordinate(area.xMax, area.yMin));
        illegalTiles.add(new Coordinate(area.xMax, area.yMax));

        // Now we're ready to do the stuff
        // The barriers need the most valid tiles, so these before mirrors.
        ArrayList<PathCoordinate> unbreakableBarriers = getUnbreakableBarriers(beamPathTiles, illegalTiles, anchors);

        // Breakable barriers can intersect the lightbeam (only perpendicularly), so they need a few less tiles.
        ArrayList<PathCoordinate> breakableBarriers = getBreakableBarriers(beamPathTiles, illegalTiles, anchors);

        // Finally, the easy one
        ArrayList<PathCoordinate> allBarriers = new ArrayList<>();
        allBarriers.addAll(breakableBarriers);
        allBarriers.addAll(unbreakableBarriers);
        ArrayList<MirrorConfiguration> mirrors = getMirrors(solution, allBarriers, illegalTiles, beamPathTiles, players);


        return new HetPuzzleLayout(unbreakableBarriers, breakableBarriers, mirrors, beamPathTiles);
    }

    private static WorldObject placeMirror(int id, int x, int y, int height, int face){
        return new WorldObject(id, x, y, height, face, 10, id, Integer.MAX_VALUE, false);
    }

    private static WorldObject placeBarrier(int id, int x, int y, int height, int face){
        return new WorldObject(id+1, x, y, height, face, 10, id, 4, false);
    }
    
    private static ArrayList<PathCoordinate> getUnbreakableBarriers(ArrayList<PathCoordinate> lightbeam, ArrayList<Coordinate> illegalTiles, ArrayList<Coordinate> anchors){
        return getBarrierTiles(false, 8, illegalTiles, lightbeam, anchors);
    }

    private static ArrayList<PathCoordinate> getBreakableBarriers(ArrayList<PathCoordinate> lightbeam, ArrayList<Coordinate> illegalTiles, ArrayList<Coordinate> anchors) {
        return getBarrierTiles(true, 4, illegalTiles, lightbeam, anchors);
    }

    private static ArrayList<PathCoordinate> getBarrierTiles(boolean breakable, int n, ArrayList<Coordinate> illegalTiles, ArrayList<PathCoordinate> lightbeam, ArrayList<Coordinate> anchors){
        ArrayList<PathCoordinate> barrierTiles = new ArrayList<>();
        int barriers = 0;
        int tries = 0;
        while (barriers <= n && tries++ < 200){
            int length = Misc.random(2)+3;
            int direction = Misc.random(3);
            int offset = Misc.random(length-1);
            int x;
            int y;
            if (breakable && barriers == 0){
                Coordinate c = lightbeam.get(1+Misc.random(lightbeam.size()-3));
                if (anchors.stream().anyMatch(coordinate -> coordinate.getX() == c.getX() && coordinate.getY() == c.getY())){
                    continue;
                }
                int[] perpendiculars;
                // If this is true, then the lightbeam has an adjacent tile to the west or east, meaning we should face north/south
                if (lightbeam.stream().anyMatch(coordinate -> Math.abs(c.getX()-coordinate.getX()) <= 1 && coordinate.getX() != c.getX() && coordinate.getY() == c.getY())){
                    perpendiculars = Directions.perpendicular(Directions.EAST);
                } else {
                    perpendiculars = Directions.perpendicular(Directions.NORTH);
                }
                direction = perpendiculars[Misc.random(1)];
                x = c.getX();
                y = c.getY();
                int[] delta = Directions.DELTAS.get(direction);
                x -= delta[0] * offset;
                y -= delta[1] * offset;
            } else {
                int xMult = Misc.random(1) == 0 ? 1 : -1;
                x = 3680+xMult*5;
                int yMult = Misc.random(1) == 0 ? 1 : -1;
                y = 5280+yMult*2;

                y += Misc.random(7) * yMult;

                x += Misc.random(3) * xMult;
            }


            int[] delta = Directions.DELTAS.get(direction);
            boolean place = true;
            for (int i = 0; i <= length; i++){
                int placeX = x+delta[0]*i;
                int placeY = y+delta[1]*i;
                if (!area.contains(placeX, placeY) || exclusion.contains(placeX, placeY)){
                    if (i < 4){
                        place = false;
                    } else {
                        length = i-1;
                    }
                }
                int finalDirection = direction;
                if (illegalTiles.stream().anyMatch(coordinate -> coordinate.getX() == placeX && coordinate.getY() == placeY)
                        || (!breakable && lightbeam.stream().anyMatch(coordinate -> coordinate.getX() == placeX && coordinate.getY() == placeY))
                        || (breakable && anchors.stream().anyMatch(coordinate -> coordinate.getX() == placeX && coordinate.getY() == placeY))
                        || (breakable && lightbeam.stream().anyMatch(coordinate -> coordinate.getX() == placeX && coordinate.getY() == placeY && (coordinate.direction == finalDirection || Directions.opposite(coordinate.direction) == finalDirection)))){
                    if (i < 4){
                        place = false;
                    } else {
                        length = i-1;
                    }
                }
            }

            if (place){
                barriers++;
                for (int i = 0; i <= length; i++) {
                    int placeX = x + delta[0] * i;
                    int placeY = y + delta[1] * i;
                    PathCoordinate coord = new PathCoordinate(placeX, placeY, direction);
                    barrierTiles.add(coord);
                    illegalTiles.add(coord);
                    for (int dir : Directions.perpendicular(direction)) {
                        int[] perp = Directions.DELTAS.get(dir);
                        illegalTiles.add(new Coordinate(placeX + perp[0], placeY + perp[1]));
                    }
                    if (((placeX == x && placeY == y) || (placeX == x + delta[0] * length && placeY == y + delta[1] * length))) {
                        illegalTiles.add(new Coordinate(placeX + delta[0], placeY + delta[1]));
                        illegalTiles.add(new Coordinate(placeX - delta[0], placeY - delta[1]));
                    }
                }
            }
        }
        return barrierTiles;
    }

    private static ArrayList<MirrorConfiguration> getMirrors(ArrayList<MirrorConfiguration> solution, ArrayList<PathCoordinate> barriers, ArrayList<Coordinate> illegalTiles, ArrayList<PathCoordinate> lightbeam, int players) {
        ArrayList<MirrorConfiguration> mirrors = new ArrayList<>();
        int count = 0;
        while (count < 7){
            int x = Misc.random(area.xMax - area.xMin) + area.xMin;
            int y = Misc.random(area.yMax - area.yMin) + area.yMin;
            if ((barriers.stream().noneMatch(coordinate -> coordinate.getX() == x && coordinate.getY() == y) &&
                lightbeam.stream().noneMatch(coordinate -> coordinate.getX() == x && coordinate.getY() == y)) &&
                !exclusion.contains(x, y)){
                int id = ObjectId.MIRROR_45455;
                if (count >= 3) {
                    id = ObjectId.MIRROR_DIRTY_45457;
                }
                if (count >= 3 + players) {
                    id = ObjectId.MIRROR_45456;
                }
                mirrors.add(new MirrorConfiguration(x, y, Misc.random(3), id));
                for (int[] delta : Directions.DELTAS){
                    illegalTiles.add(new Coordinate(x + delta[0], y + delta[1]));
                }
                count++;
            }
        }

        // Mirror which lies in the solution beam path last, if the RNG chance is met, or if its required (4 mirror solution)
        int mirrorToPlace = Misc.random(solution.size()-3)+1;
        if (solution.size() < 4 && Misc.random(2) != 0){
            mirrorToPlace = -1;
        }
        if (mirrorToPlace > 0){
            MirrorConfiguration info = solution.get(mirrorToPlace);
            mirrors.add(info);
        }
        return mirrors;
    }

    public static class HetPuzzleLayout {
        public final ArrayList<PathCoordinate> unbreakableBarriers;
        public final ArrayList<PathCoordinate> breakableBarriers;
        public final ArrayList<MirrorConfiguration> mirrors;
        public final ArrayList<PathCoordinate> lightbeamTiles;

        public HetPuzzleLayout(ArrayList<PathCoordinate> unbreakableBarriers, ArrayList<PathCoordinate> breakableBarriers, ArrayList<MirrorConfiguration> mirrors, ArrayList<PathCoordinate> lightbeamTiles) {
            this.unbreakableBarriers = unbreakableBarriers;
            this.breakableBarriers = breakableBarriers;
            this.mirrors = mirrors;
            this.lightbeamTiles = lightbeamTiles;
        }
    }

    public static class HetPuzzleObjects {
        public final ArrayList<WorldObject> unbreakableBarriers;
        public final ArrayList<WorldObject> breakableBarriers;
        public final ArrayList<WorldObject> mirrors;

        public HetPuzzleObjects(){
            this.unbreakableBarriers = new ArrayList<>();
            this.breakableBarriers = new ArrayList<>();
            this.mirrors = new ArrayList<>();
        }
    }

    private static class MirrorConfiguration {
        public final int x;
        public final int y;
        public final int face;
        public final int id;

        public MirrorConfiguration(int x, int y, int face, int id) {
            this.x = x;
            this.y = y;
            this.face = face;
            this.id = id;
        }
    }

    // Extension was just to make conversion easy
    // I guess it is a coordinate, in some sense
    private static class PathCoordinate extends Coordinate {
        public final int x;
        public final int y;
        public final int direction;

        private PathCoordinate(int x, int y, int direction) {
            super(x, y);
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        HetPuzzleLayout layout = makeLayout(0);
        int tries = 0;
        while(tries++ < 100 && (layout.breakableBarriers.size() == 0 || layout.unbreakableBarriers.size() == 0)){
            layout = makeLayout(0);
        }
        ArrayList<PathCoordinate> barrierTiles = layout.unbreakableBarriers;
        System.out.println("Layout tries: " + tries);
        System.out.println("Run-time (ms): " + (System.currentTimeMillis() - start) + "");


        for (int y = area.yMin; y <= area.yMax; y++){
            String line = "";
            for (int x = area.xMin; x <= area.xMax; x++){
                if (exclusion.contains(x, y)){
                    line += "X";
                    continue;
                }
                int finalX = x;
                int finalY = y;
                if (barrierTiles.stream().anyMatch(tile -> finalX == tile.getX() && finalY == tile.getY())){
                    line += "U";
                    continue;
                }
                if (Arrays.stream(layout.breakableBarriers.toArray(new Coordinate[0])).anyMatch(tile -> finalX == tile.getX() && finalY == tile.getY())){
                    line += "B";
                    continue;
                }
                if (Arrays.stream(layout.mirrors.toArray(new MirrorConfiguration[0])).anyMatch(mirror -> finalX == mirror.x && finalY == mirror.y)){
                    line += "M";
                    continue;
                }
                if (Arrays.stream(layout.lightbeamTiles.toArray(new Coordinate[0])).anyMatch(tile -> finalX == tile.getX() && finalY == tile.getY())){
                    line += "L";
                    continue;
                }
                line += " ";
            }
            System.out.println(line);
        }
    }
}
