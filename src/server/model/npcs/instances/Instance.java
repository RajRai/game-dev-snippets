package server.model.npcs.instances;

import server.model.Entity;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.PlayerRights;
import server.util.TimedEvent;
import server.world.Clan;
import server.world.bounding.LocalArea;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Instance {

    public static class ParticipantInfo {
        public Player player;
        public int playerId;
        private long lastTimePresent;

        private ParticipantInfo(Player player){
            this.player = player;
            this.playerId = player.getPlayerId();
            this.lastTimePresent = System.currentTimeMillis();
        }
    }

    protected Player owner;
    protected final InstanceId boss;
    protected final InstanceType type;
    public final LocalArea localArea;
    public Clan linkedClan;
    protected int ownerId;
    protected List<ParticipantInfo> participants = new ArrayList<>();
    protected final int heightLevel;
    protected final int respawnTime;

    protected long despawnInitiated;

    public Instance(Player owner, InstanceId boss, InstanceType type, int heightLevel) {
        this(owner, boss, type, heightLevel, 30_000);
    }

    public Instance(Player owner, InstanceId boss, InstanceType type, int heightLevel, int respawnTime) {
        this.owner = owner;
        if(owner != null) {
            this.ownerId = owner.playerId;
        } else {
            this.ownerId = -1;
        }
        this.boss = boss;
        this.type = type;
        this.localArea = boss.worldArea.localize(heightLevel);
        this.heightLevel = heightLevel;
        this.respawnTime = respawnTime;

        if(owner != null) {
            addParticipant(owner);
        }

        InstanceManager.addInstance(this);
    }

    public void process(){
        // Update presence times
        for (ParticipantInfo info : participants){
            if (localArea.contains(info.player) && !info.player.afk){
                info.lastTimePresent = System.currentTimeMillis();
            }
        }

        // Remove people who should be removed
        participants.stream()
            .filter(this::shouldRemove)
            .collect(Collectors.toSet())
            .forEach(info -> InstanceManager.removePlayer(info.player, this));

        if (owner == null && type != InstanceType.PUBLIC){
            newOwnerNeeded();
        }
    }

    public boolean canOwnerTransfer(){
        return true;
    }

    public void newOwnerNeeded(){
        if (!canOwnerTransfer()){
            InstanceManager.removeInstance(this);
            return;
        }
        participants.removeIf(info -> info.player == null);
        if (participants.size() > 0 && participants.get(0).player != null){
            Player newOwner = participants.get(0).player;
            if (newOwner != null){
                owner = newOwner;
                ownerId = newOwner.getPlayerId();
                owner.sendMessage("You are now the instance owner.");
            }
        } else {
            // No new owner possible
            InstanceManager.removeInstance(this);
        }
    }

    public void linkClan(Clan clan){
        this.linkedClan = clan;
    }

    public boolean isClanLinked(){
        return this.linkedClan != null;
    }

    public Player getOwner() {
        return owner;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public boolean isOwner(Player c) {
        if(c == null) {
            return false;
        }
        if(c == owner) {
            return true;
        }
        if(c.playerId == ownerId) {
            owner = c;
            return true;
        }
        return false;
    }

    public InstanceId getInstanceId() {
        return boss;
    }

    public InstanceType getType() {
        return type;
    }

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public void addParticipant(Player p) {
        if(p != null && !hasParticipant(p)) {
            participants.add(new ParticipantInfo(p));
        }
    }

    public boolean hasParticipant(Player p){
        return participants.stream().anyMatch(info -> info.playerId == p.getPlayerId());
    }

    public void onPlayerLogin(Player p) {
        for (ParticipantInfo info : participants){
            if (info.playerId == p.getPlayerId()){
                info.player = p;
            }
        }
    }

    public int getHeightLevel() {
        return heightLevel;
    }

    public boolean shouldDespawn() {
        //Never despawn public instances
        if(type == InstanceType.PUBLIC) {
            return false;
        }

        return participants.stream().allMatch(this::shouldRemove);
    }

    public boolean isOwnerPresent() {
        return isPlayerPresent(owner);
    }

    public boolean shouldRemove(ParticipantInfo info){
        return System.currentTimeMillis() - info.lastTimePresent >= 300_000;
    }

    private boolean isInInstance(Entity e){
        return this.localArea.contains(e);
    }

    public boolean isInBoundary(int x, int y, int h) {
        return this.localArea.contains(x, y, h);
    }

    public boolean isPlayerPresent(Player p) {
        return p != null && !p.isDead && p.heightLevel == heightLevel && isInInstance(p) && hasParticipant(p);
    }

    public boolean playerCanEnter(Player p) {
        return p != null && hasParticipant(p)
            || type == InstanceType.PUBLIC;
    }

    public void playerEntered(Player p) {
        if(noPlayersPresent()) {
            destroy();
            spawn(true);
        }
        if (!localArea.contains(p)){
            teleportInside(p);
        }
        addParticipant(p);
        p.getPA().closeAllWindows();
    }

    public boolean noPlayersPresent() {
        for(Player p : PlayerHandler.players) {
            if(isPlayerPresent(p) && !PlayerRights.isHonor(p.playerRights)) {
                return false;
            }
        }
        return true;
    }

    public void initiateRespawn() {
        new TimedEvent("InstanceRespawn") {
            @Override
            public void fire() {
                spawn(false);
            }
        }.schedule(respawnTime);
    }

    public abstract void teleportInside(Player p);

    public abstract void spawn(boolean firstSpawn);

    public void destroy(){
        for (NPC npc : NPCHandler.npcs){
            if (shouldDespawnOnDestroy(npc)){
                npc.dispose();
            }
        }
    }

    public boolean shouldDespawnOnDestroy(NPC npc){
        return localArea.contains(npc) && !npc.isPet;
    }

    public void onDeath(NPC processedNpc){
        processedNpc.onDeath();
    }

    public void disband() {
        InstanceManager.removeInstance(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return heightLevel == instance.heightLevel && boss == instance.boss && type == instance.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boss, type, heightLevel);
    }
}
