package server.model.npcs.instances.tombs_of_amascut_WIP.het;

import server.model.combat.HitIcon;
import server.model.items.ItemId;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.npcs.NpcId;
import server.model.npcs.instances.tombs_of_amascut_WIP.TombsInstance;
import server.model.npcs.instances.tombs_of_amascut_WIP.TombsNPC;
import server.model.npcs.instances.tombs_of_amascut_WIP.TombsRoom;
import server.model.objects.Lightbeam;
import server.model.objects.ObjectId;
import server.model.objects.WorldObject;
import server.model.players.Player;
import server.model.players.skills.Mining;
import server.model.players.skills.Skill;
import server.observers.Observers;
import server.observers.players.ActionObserver;
import server.observers.players.MiningObserver;
import server.util.Misc;
import server.util.TimedEvent;
import server.util.walking.Directions;
import server.world.ObjectManager;
import server.world.bounding.WorldArea;

import java.util.Collection;


public class HetPuzzle extends TombsRoom implements ActionObserver, MiningObserver {

    private HetPuzzleBuilder.HetPuzzleObjects puzzleObjects;
    public static int SEAL_OBJECT_ID = NpcId.HETS_SEAL_WEAKENED_11705;

    /*
     * Puzzle controller
     */
    public HetPuzzle(TombsInstance instance, WorldArea worldArea) {
        super(instance, worldArea);
        lightbeamEvent.schedule(0);
        Observers.actionObservers.add(this);
        Observers.miningObservers.add(this);
    }

    @Override
    protected void setup(Player c) {
        seal = new Seal(NPCHandler.getFreeSlot(), NpcId.HETS_SEAL_WEAKENED_11705, 3679, 5279, instance.getHeightLevel());
    }

    @Override
    protected void destroy() {
        super.destroy();
        seal.dispose();
        lightbeamEvent.cancel();
        puzzleObjects.breakableBarriers.forEach(ObjectManager::removeObject);
        puzzleObjects.unbreakableBarriers.forEach(ObjectManager::removeObject);
        puzzleObjects.mirrors.forEach(ObjectManager::removeObject);
        Observers.actionObservers.remove(this);
        Observers.miningObservers.remove(this);
    }

    @Override
    protected void teleportInside(Player c) {
        instance.teleportWithBlackFade(c, 3698, 5280, 0);
    }

    @Override
    protected void start(Player c) {
        puzzleObjects = HetPuzzleBuilder.build(instance.getHeightLevel(), instance.getParticipants().size());
        lightbeamEvent.schedule(0);
    }

    /*
     * Het's seal (protected/weakened)
     */
    public Seal seal;

    public class Seal extends TombsNPC {
        public Seal(int npcId_, int npcType, int absX_, int absY_, int heightLevel) {
            super(npcId_, npcType, absX_, absY_, heightLevel, 0);
        }

        @Override
        public boolean checkPlayerAttack(Player c, boolean notify) {
            if (getNpcType() == NpcId.HETS_SEAL_PROTECTED_11704){
                if (notify){
                    c.sendMessage("The Seal is immune to attacks.");
                }
                return false;
            }
            if (!c.getMining().isPickaxe(c.playerEquipment[c.playerWeapon])){
                if (notify) {
                    c.sendMessage("The Seal can only be damaged by a pickaxe...");
                }
                return false;
            }
            return true;
        }

        @Override
        protected boolean canFace() {
            return false;
        }

        @Override
        public void onDeath() {
            super.onDeath();
            HetPuzzle.this.finish();
        }
    }

    /*
     * Observer methods
     */
    @Override
    public String getName() {
        return "HetPuzzle";
    }

    @Override
    public boolean onItemOnObject(Player c, int itemId, int obId, int obX, int obY, int obFace) {
        // todo: mirror cleaning
        return false;
    }

    @Override
    public boolean onObjectOption(Player c, WorldObject object, int obId, int obX, int obY, int obFace, int entry) {
        if (object == null){
            return false;
        }
        if (obId == ObjectId.MIRROR_45455){
            if (entry == 0){ // pick-up
                if (c.getItems().freeSlots() == 0){
                    c.sendMessage("You don't have enough free space for this.");
                    return true;
                }
                ObjectManager.removeObject(object);
                c.getItems().addItem(ItemId.MIRROR_27296);
                c.startAnimation(827);
            } else if (entry == 1){ // rotate-clockwise
                object.setFace((obFace+1)%4);
            } else if (entry == 2) { // rotate-counterclockwise
                object.setFace((obFace+3)%4);
            } else if (entry == 3) { // push
                int[] delta = Directions.getClippedDelta(c.absX, c.absY, obX, obY);
                if (c.canWalkTo(obX, obY, delta[0], delta[1])){
                    object.setX(obX + delta[0]);
                    object.setY(obY + delta[1]);
                }
            }
            return true;
        }
        if (isBreakableBarrier(obId)){
            c.getMining().startMining(obId, obX, obY, obFace, -1, 1, 100);
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemOption(Player c, int itemId, int slot, int entry) {
        if (itemId == ItemId.MIRROR_27296){
            if (entry == 0 || entry == 3){
                for (WorldObject object : ObjectManager.worldObjects){
                    if (object.getObjectX() == c.absX && object.getObjectY() == c.absY && object.getHeight() == c.heightLevel){
                        c.sendMessage("You cannot place that here.");
                        return true;
                    }
                }
                c.startAnimation(827);
                c.getItems().deleteItem(itemId, slot, 1);
                puzzleObjects.mirrors.add(new WorldObject(ObjectId.MIRROR_45455, c.absX, c.absY, c.heightLevel, 0, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNPCOption(Player c, NPC npc, int npcIndex, int entry) {
        if (npc == seal){
            c.getMining().startMining(SEAL_OBJECT_ID, seal.absX, seal.absY, 0, -1, 1, 0);
            return true;
        }
        return false;
    }

    @Override
    public int onOreMined(Player c, Mining mining, int objectId, int objectX, int objectY, int objectFace, int oreId, int levelReq, int xp) {
        if (HetPuzzle.isBreakableBarrier(objectId)){
            if (Misc.random(2) == 0){
                WorldObject object = ObjectManager.getObject(objectId, objectX, objectY, c.heightLevel);
                if (object != null){
                    object.setIds(ObjectId.BARRIER_45464 + 2, ObjectId.NOTHING_6951, Integer.MAX_VALUE);
                    object.setWalkable(true);
                }
                mining.resetMining(true);
                c.getPA().addSkillXP(xp, Skill.MINING);
            }
        }
        if (objectId == HetPuzzle.SEAL_OBJECT_ID){
            seal.dealDamage(10, c, HitIcon.MELEE); // todo: damage by level, random variance
            return 1200;
        }
        return -1;
    }

    /*
     * Lightbeam/Mirror mechanics
     */
    public TimedEvent lightbeamEvent = new TimedEvent("TOAFireLightbeam") {

        private Lightbeam lightbeam = null;

        @Override
        public void fire() {
            if (playerStarted.size() <= 0){
                return;
            }
            if (lightbeam != null){
                lightbeam.destroy();
            }
            lightbeam = new HetLightbeam(3676, 5280, HetPuzzle.this.instance.getHeightLevel(), Directions.WEST, playerStarted, Math.max(5, instance.raidLevel / 20));

            schedule(4800);
        }

        @Override
        public void cancel() {
            lightbeam.destroy();
            super.cancel();
        }
    };

    private static class HetLightbeam extends Lightbeam {
        public HetLightbeam(int startX, int startY, int height, int direction, Collection<Player> showTo, int damage){
            super(startX, startY, height, direction, showTo, damage);
        }

        @Override
        protected boolean canPassThrough(WorldObject collided) {
            if (collided.getObjectId() == ObjectId.SHIELDED_STATUE_45485){
                // todo: activate seal damage phase
            }

            return super.canPassThrough(collided);
        }
    }

    /*
     * Utils
     */

    public static boolean isBreakableBarrier(int obId){
        return obId == ObjectId.BARRIER_45464 || obId == ObjectId.BARRIER_45462;
    }
}
