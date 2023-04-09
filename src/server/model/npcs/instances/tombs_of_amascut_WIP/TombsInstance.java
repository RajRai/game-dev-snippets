package server.model.npcs.instances.tombs_of_amascut_WIP;

import server.model.npcs.NPCHandler;
import server.model.npcs.NpcId;
import server.model.npcs.instances.*;
import server.model.npcs.instances.tombs_of_amascut_WIP.het.AkkhaRoom;
import server.model.npcs.instances.tombs_of_amascut_WIP.het.HetPuzzle;
import server.model.objects.ObjectId;
import server.model.objects.WorldObject;
import server.model.players.Player;
import server.observers.Observers;
import server.observers.players.ActionObserver;
import server.world.WorldMap;

public class TombsInstance extends Instance {

    public int raidLevel = 0;

    TombsPath currentPath = null;
    TombsRoom currentRoom = null;

    private final TombsPath pathOfHet = new TombsPath(new HetPuzzle(this, WorldMap.HET_PUZZLE), new AkkhaRoom(this, WorldMap.HET_FIGHT), this) {
        @Override
        protected void spawnOsmumten() {
            NPCHandler.newInstanceNPC(NpcId.OSMUMTEN_11687, 3673, 5407, 1, 0, TombsInstance.this);
        }

        @Override
        public boolean isTeleportCrystal(int obId) {
            return obId == ObjectId.TELEPORT_CRYSTAL_45866;
        }

        @Override
        public void enterFight(Player c) {
            c.getPA().movePlayer(3672, 5407, heightLevel+1);
        }

        @Override
        public boolean isPuzzleExit(int obId) {
            return obId == ObjectId.ENTRY_45131;
        }
    };

    public TombsInstance(Player owner, InstanceType type, int heightLevel) {
        super(owner, InstanceId.TOA, type, heightLevel);
        Observers.actionObservers.add(doorObserver);
    }

    // todo: add party system + action observer to make an instance given a pre-formed party
    public static void load(){
        PrivatePortal.Builder.create()
            .obX(3357)
            .obY(9112)
            .obId(ObjectId.ENTRY_46089)
            .defaultActionObserver(0, InstanceId.TOA)
            .build();
    }

    private static void passBarrier(Player c, int obX, int obY){
        int[] delta = new int[]{obX-c.absX, obY-c.absY}; // Get the x and y distance between us and the barrier
        if (Math.abs(delta[0]) + Math.abs(delta[1]) > 1){ // If it's more than 1 tile cardinally, we're too far
            return;
        }
        c.getPA().walkTo(delta[0] * 2, delta[1] * 2, true); // Walk in that direction 2 tiles
    }

    private final ActionObserver doorObserver = new ActionObserver() {
        @Override
        public String getName() {
            return "TOADoorHandling";
        }

        @Override
        public boolean onObjectOption(Player c, WorldObject object, int obId, int obX, int obY, int obFace, int entry) {
            if (!localArea.contains(obX, obY, c.heightLevel)){
                return false;
            }
            if (currentPath != null){
                if (obId == ObjectId.BARRIER_45135 || currentPath.isTeleportCrystal(obId)){
                    if (currentRoom != null){
                        if (currentRoom.playerStarted.contains(c)){
                            c.sendMessage("You can't do that right now.");
                            return true;
                        } else {
                            currentRoom.playerStarted.add(c);
                        }
                        if (currentRoom.playerStarted.size() == 1){
                            currentRoom.start(c);
                        }
                    }
                }
                if (currentPath.isTeleportCrystal(obId)){
                    currentPath.enterFight(c);
                    return true;
                }
                if (currentPath.isPuzzleExit(obId)){
                    currentPath.boss.enter(c);
                    return true;
                }
            }
            if (obId == ObjectId.BARRIER_45135){
                if (entry == 0){
                    // todo: show warning dialogue
                    passBarrier(c, obX, obY);
                } else if (entry == 1){
                    passBarrier(c, obX, obY);
                }
                return true;
            }
            if (obId == ObjectId.EXIT_45128 && entry == 0){
                c.getDH().build().sendTitledOptions(
                    "Leave the raid?",
                    "Yes.", () -> {
                        InstanceManager.removePlayer(c, TombsInstance.this);
                        c.getPA().closeAllWindows();
                    },
                    "Never mind.", () -> c.getPA().closeAllWindows()
                ).run();
                return true;
            }
            switch (obId){
                case ObjectId.PATH_OF_HET_46164:
                    if (currentPath == null || currentPath == pathOfHet){
                        pathOfHet.enter(c);
                    } else {
                        sendWrongPath(c);
                    }
                    return true;
            }
            return false;
        }
    };

    private static void sendWrongPath(Player c){
        c.sendMessage("You can't enter this path right now.");
    }

    @Override
    public void destroy() {
        super.destroy();
        Observers.actionObservers.remove(doorObserver);

        pathOfHet.puzzle.destroy();
    }

    // todo
    public void teleportWithBlackFade(Player c, int x, int y, int zOffset){
        c.getPA().spellTeleport(x, y, heightLevel+zOffset);
    }

    @Override
    public void teleportInside(Player p) {
        teleportWithBlackFade(p, 3551, 5162, 0);
    }

    @Override
    public void spawn(boolean firstSpawn) {}
}
