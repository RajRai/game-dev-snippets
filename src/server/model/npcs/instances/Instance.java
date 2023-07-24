package server.model.npcs.instances;

import server.Server;
import server.model.Entity;
import server.model.combat.HitIcon;
import server.model.npcs.NPC;
import server.model.npcs.NPCBuilder;
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
            this.lastTimePresent = Server.currentTime;
        }
    }

    protected Player owner;
    public final InstanceId boss;
    public final InstanceType type;
    public final LocalArea localArea;
    public Clan linkedClan;
    protected int ownerId;
    protected List<ParticipantInfo> participants = new ArrayList<>();
    protected final int heightLevel;
    public boolean invalidateKilltimes;

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
            if (isInBoundary(info.player) && !info.player.afk){
                info.lastTimePresent = Server.currentTime;
            }
        }

        // Remove people who should be removed
        participants.stream()
            .filter(this::shouldRemove)
            .collect(Collectors.toSet()) // Avoids ConcurrentModificationException
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
        if (participants.size() > 0 && participants.get(0).player != null){
            Player newOwner = participants.get(0).player;
            if (newOwner != null){
                owner = newOwner;
                ownerId = newOwner.getPlayerId();
                owner.sendMessage("You are now the instance owner.");
            }
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

    public List<Player> getPlayers() {
        return participants.stream().map(info -> info.player).collect(Collectors.toList());
    }

    public List<ParticipantInfo> getValidatedParticipants(){
        return participants.stream()
            .filter(info -> info.player != null && isInInstance(info.player) && !info.player.isInvisible())
            .collect(Collectors.toList());
    }

    // todo: replaces calls of getValidatedParticipants with this
    public List<Player> getValidatedPlayers(){
        return getValidatedParticipants().stream()
            .map(info -> info.player)
            .collect(Collectors.toList());
    }

    public void addParticipant(Player p) {
        Instance instance = InstanceManager.getInstance(p);
        if (instance != null && instance != this){
            InstanceManager.removePlayer(p, instance);
        }
        if (hasParticipant(p)){
            participants.forEach(info -> {
                if (info.playerId == p.getPlayerId()){
                    info.player = p;
                }
            });
        }
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
        return isInInstance(owner);
    }

    public boolean shouldRemove(ParticipantInfo info){
        return Server.currentTime - info.lastTimePresent >= timeAllowedAwayMs(type);
    }

    protected long timeAllowedAwayMs(InstanceType type) {
        switch (type){
            case SOLO:
            case PUBLIC:
                return 6_000;
            case PRIVATE:
                return 60_000 * 15;
            default:
                return 0;
        }
    }

    public boolean isInBoundary(Entity e){
        Player p = e instanceof Player ? (Player) e : null;
        return e != null && (isInBoundary(e.absX, e.absY, e.heightLevel) || (p != null && isInBoundary(p.teleX, p.teleY, p.teleHeight)));
    }

    public boolean isInBoundary(int x, int y, int h) {
        return this.localArea.contains(x, y, h);
    }

    public boolean isInInstance(Player p) {
        return p != null && isInBoundary(p) && hasParticipant(p);
    }

    public boolean playerCanEnter(Player p) {
        return boss.enabled;
    }

    protected boolean shouldTeleport(Player p){
        return !isInBoundary(p);
    }

    public void quietEnter(Player p){
        if (shouldTeleport(p)){
            teleportInside(p);
        } else {
            p.getPA().movePlayer(p.absX, p.absY, heightLevel);
        }
        p.getPA().closeAllWindows();
    }

    public void playerEntered(Player p) {
        if (!playerCanEnter(p)){
            if (isInBoundary(p)) {
                boss.moveToPortalArea.accept(p);
            }
            return;
        }
        if(noPlayersPresent() || (type == InstanceType.SOLO && p.getPlayerId() == ownerId)) {
            reset();
        }
        quietEnter(p);
        addParticipant(p);
    }

    public void playerEnteredWithValidation(Player p){
        if (!playerCanEnter(p)){
            p.sendMessage("You can't enter right now.");
            return;
        }
        playerEntered(p);
    }

    public boolean noPlayersPresent() {
        for(Player p : PlayerHandler.players) {
            if(isInInstance(p) && !PlayerRights.isHonor(p.playerRights)) {
                return false;
            }
        }
        return true;
    }

    public void initiateRespawn() {
        invalidateKilltimes = false;
        new TimedEvent("InstanceRespawn") {
            @Override
            public void fire() {
                spawn();
            }
        }.schedule(respawnTime);
    }

    public abstract void teleportInside(Player p);

    public abstract void spawn();

    public void reset(){
        despawn();
        if (boss.enabled){
            spawn();
        }
    }

    public void despawn(){
        for (NPC npc : NPCHandler.npcs){
            if (shouldDespawnNPC(npc)){
                npc.dispose();
            }
        }
    }

    public boolean shouldDespawnNPC(NPC npc){
        return isInBoundary(npc) && !npc.isPet;
    }

    public void onDeath(NPC processedNpc){
        processedNpc.onDeath();
    }

    public void disband() {
        InstanceManager.removeInstance(this);
    }

    public NPCBuilder getNpcBuilder() {
        return new NPCBuilder()
            .instance(this)
            .height(heightLevel)
            .player(type == InstanceType.SOLO ? owner : null);
    }

    public boolean invalidateKilltimes() {
        return invalidateKilltimes || boss.invalidateKilltimes;
    }

    public boolean allowGuestAttacks() {
        return false;
    }

    public void onDamageTaken(NPC npc, int damage, Player c, HitIcon hitIcon){
        if (c != null && c.playerRights >= PlayerRights.ADMIN && damage > 0){
            invalidateKilltimes = true;
        }
    }

    public boolean safeDeath() {
        return false;
    }

    public boolean safeForHC(){
        return false;
    }

    public boolean npcRespawns(NPC npc) {
        return true;
    }

    public boolean moveOutsideOnDeath(Player c) {
        return safeDeath();
    }

    public boolean allowTransform(Player c){
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return heightLevel == instance.heightLevel && boss == instance.boss;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boss, heightLevel);
    }
}
