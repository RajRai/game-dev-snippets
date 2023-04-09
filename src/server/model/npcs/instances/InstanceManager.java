package server.model.npcs.instances;

import server.model.npcs.Coordinate;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.observers.Observers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InstanceManager {

    public static final HashSet<Instance> instances = new HashSet<>();
    public static final List<PrivatePortal> portals = new ArrayList<>();

    // Public instances
    public static final Instance NEX_PUBLIC = InstanceId.NEX.builder.build(null, InstanceType.PUBLIC, 0);

    public static void process() {
        for(Instance instance : instances) {
            instance.process();
            if(instance.shouldDespawn()) {
                removeInstance(instance);
            }
        }
    }

    public static void addInstance(Instance instance) {
        instances.add(instance);
    }

    public static HashSet<Instance> getInstances() {
        return instances;
    }

    public static void removeInstance(Instance instance) {
        if(instance != null) {
            for (Instance.ParticipantInfo info : instance.participants){
                Player c = info.player;
                if (c == null){
                    continue;
                }
                if (instance.localArea.contains(c)){
                    c.getPA().movePlayer(instance.boss.outside);
                    c.getPA().closeAllWindows();
                    c.sendMessage("The instance was disbanded and you were moved outside.");
                } else {
                    c.sendMessage("Your private instance was disbanded.");
                }
            }
            instance.destroy();
            instances.remove(instance);
        }
    }

    public static Instance createInstance(Player p, InstanceId boss, InstanceType type) {
        return createInstance(p, boss, type, PlayerHandler.getAvailableHeight(p, boss.heightOffset));
    }

    public static Instance createInstance(Player p, InstanceId boss, InstanceType type, int heightLevel) {
        return boss.builder.build(p, type, heightLevel);
    }

    public static void teleportToPortalArea(Player p, InstanceId boss) {
        Coordinate to = boss.outside;
        p.getPA().movePlayer(to.getX(), to.getY(), to.getZ());
    }

    public static void checkInstanceOnLogin(Player p) {
        Instance instance = null;
        InstanceId boss = null;
        for(Instance inst : instances) {
            if(inst.localArea.contains(p)) {
                boss = inst.boss;
                if(inst.playerCanEnter(p)) {
                    instance = inst;
                    inst.onPlayerLogin(p);
                    inst.playerEntered(p);
                }
                break;
            }
        }
        if (boss == null){
            for (InstanceId b : InstanceId.values()){
                if (b.worldArea.contains(p)){
                    boss = b;
                    break;
                }
            }
        }
        if(instance == null && boss != null) {
            teleportToPortalArea(p, boss);
        }
    }

    public static Instance getInstance(Player c) {
        for (Instance instance : instances){
            if (instance.getParticipants().stream().anyMatch(info -> info.playerId == c.getPlayerId())){
                return instance;
            }
        }
        return null;
    }

    public static void removePortal(PrivatePortal portal){
        if (portals.remove(portal) && portal.actionObserver != null){
            Observers.actionObservers.remove(portal.actionObserver);
        }
    }

    public static void removePlayer(Player c, Instance instance){
        instance.participants.removeIf(info -> info.player == c);
        if (c == instance.owner){
            instance.newOwnerNeeded();
        }
        if (instance.localArea.contains(c)){
            c.getPA().movePlayer(instance.boss.outside);
            c.sendMessage("You leave the instance.");
            c.getPA().closeAllWindows();
        }
    }
}
