package server.model.npcs.instances;

import server.Server;
import server.ServerLogger;
import server.model.npcs.instances.nex.NexInstance;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.PlayerRights;
import server.model.players.dialogue.ddm.MenuLayer;
import server.observers.Observers;
import server.util.TextUtils;
import server.world.Clan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InstanceManager {

    public static final HashSet<Instance> instances = new HashSet<>();
    public static final List<PrivatePortal> portals = new ArrayList<>();

    // Public instances
    public static final NexInstance NEX_PUBLIC = new NexInstance(null, InstanceType.PUBLIC, 0);

    public static void process() {
        for(Instance instance : instances) {
            if(instance != null) {
                try {
                    instance.process();
                } catch (Exception e) {
                    ServerLogger.getInstance().log(Level.SEVERE, "Exception processing instance " + instance.getInstanceId() + ", type " + instance.getType()
                        + (instance.getOwner() == null ? " without owner" : " for player " + instance.getOwner().playerName + " (playerid " + instance.getOwnerId() + ")")
                        + " at height " + instance.getHeightLevel(), e);
                }
            }
        }
        try {
            instances.stream()
                .filter(Instance::shouldDespawn)
                .toList()
                .forEach(InstanceManager::removeInstance);
        } catch(Exception e) {
            ServerLogger.getInstance().log(Level.SEVERE, "Exception checking for instances to despawn", e);
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
                    teleportToPortalArea(c, instance.boss);
                    c.getPA().closeAllWindows();
                    c.sendMessage("The instance was disbanded and you were moved outside.");
                } else {
                    c.sendMessage("Your private instance was disbanded.");
                }
            }
            instance.despawn();
            if (instance.type != InstanceType.PUBLIC){
                instances.remove(instance);
            }
        }
    }

    public static Instance getOrCreate(Player p, InstanceId boss, InstanceType type){
        Instance instance = getInstance(p);
        if (instance != null && instance.getInstanceId() == boss){
            return instance;
        } else {
            return createInstance(p, boss, type);
        }
    }

    public static Instance createInstance(Player p, InstanceId boss, InstanceType type) {
        return createInstance(p, boss, type, PlayerHandler.getAvailableHeight(p, boss.heightOffset));
    }

    public static Instance createInstance(Player p, InstanceId boss, InstanceType type, int heightLevel) {
        return boss.builder.build(p, type, heightLevel);
    }

    public static void teleportToPortalArea(Player p, InstanceId boss) {
        boss.moveToPortalArea.accept(p);
    }

    public static void checkInstanceOnLogin(Player p) {
        Instance instance = null;
        InstanceId boss = null;
        for(Instance inst : instances) {
            if(inst.isInBoundary(p)) {
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
        if (c == null) return null;
        for (Instance instance : instances){
            if (instance.getParticipants().stream().anyMatch(info -> info.playerId == c.getPlayerId())){
                return instance;
            }
        }
        return null;
    }

    public static Instance getInstance(Clan clan, InstanceId boss){
        if (clan == null) return null;
        return instances.stream()
            .filter(instance -> instance.linkedClan == clan && instance.boss == boss)
            .findFirst()
            .orElse(null);
    }

    public static Instance getOrCreateClanInstance(Clan clan, Player c, InstanceId boss){
        Instance instance = getInstance(clan, boss);
        if (instance != null) return instance;
        instance = createInstance(c, boss, InstanceType.PRIVATE);
        instance.linkClan(clan);
        return instance;
    }

    public static void removePortal(PrivatePortal portal){
        if (portals.remove(portal) && portal.actionObserver != null){
            Observers.actionObservers.remove(portal.actionObserver);
        }
    }

    public static void removePlayer(Player c, Instance instance){
        instance.participants.removeIf(info -> info.player == c);
        if (c == instance.owner && instance.type != InstanceType.PUBLIC){
            instance.newOwnerNeeded();
        }
        if (instance.isInBoundary(c)){
            teleportToPortalArea(c, instance.boss);
            c.sendMessage("You leave the instance.");
            c.getPA().closeAllWindows();
        }
    }

    public static void disbandAll(InstanceId id, Player by){
        instances.stream()
            .filter(instance -> instance.boss == id)
            .forEach(instance -> disband(instance, by));
    }

    public static void disband(Instance instance, Player by){
        instance.getPlayers().forEach(player ->
            player.sendMessage("The instance was disbanded by " + TextUtils.getPlayerNameWithCrown(by) + ".")
        );
        instance.disband();
    }

    /*
     * Admin dialogue stuff
     */

    private record BossListContext(BossListType type){}
    private record BossChosenContext(InstanceId id){}
    private record InstanceChosenContext(Instance instance){}
    private record MemberChosenContext(Instance instance, Player member){}

    private enum BossListType {
        TO_INSTANCES,
        TO_OPTIONS
    }

    public static void showAdminDialogue(Player c){

        MenuLayer<Player> base = new MenuLayer<Player>(c).withContext(c); // initialize the context for the base layer

        MenuLayer<BossListContext> bossList = new MenuLayer<>(c);
        MenuLayer<BossChosenContext> bossOptionsList = new MenuLayer<>(c);
        MenuLayer<BossChosenContext> instanceList = new MenuLayer<>(c);
        MenuLayer<InstanceChosenContext> instanceOptions = new MenuLayer<>(c);
        MenuLayer<InstanceChosenContext> membersList = new MenuLayer<>(c);
        MenuLayer<MemberChosenContext> instanceMemberOptions = new MenuLayer<>(c);

        boolean hasPermissions = c.playerRights >= PlayerRights.MODERATOR || (c.playerRights >= PlayerRights.DEVELOPER && Server.isTestServer());
        boolean hasAdminPermissions = c.playerRights >= PlayerRights.SENIORMOD || (c.playerRights >= PlayerRights.DEVELOPER && Server.isTestServer());

        base
            .pagePermission(ctx -> hasPermissions)
            .forwardOption(
                ctx -> "Show instances", // A function which will generate the text on each refresh
                ctx -> base.next(bossList, new BossListContext(BossListType.TO_INSTANCES)) // Because this is a forward option, we need to give the next layer and its context
            )
            .forwardOption(
                ctx -> "Global instance options",
                ctx -> base.next(bossList, new BossListContext(BossListType.TO_OPTIONS)),
                ctx -> hasAdminPermissions // An optional Predicate<ContextType> indicating whether to show the option
            );

        bossList
            .pagePermission(ctx -> hasPermissions)
            .selectionMenu(
                (ctx) -> Arrays.stream(InstanceId.values()).toList(), // The set of data we're selecting an object from
                (ctx, id) -> InstanceId.formatName(id), // The text to label the row with
                (ctx, id) -> { // The MenuLayer to open on a click, and its context
                    BossChosenContext bossChosenContext = new BossChosenContext(id);
                    return switch (ctx.type) {
                        case TO_OPTIONS -> bossList.next(bossOptionsList, bossChosenContext);
                        case TO_INSTANCES -> bossList.next(instanceList, bossChosenContext);
                        default -> bossList.prev(); // Not needed but it's just an example
                    };
                }
            );

        bossOptionsList
            .pagePermission(ctx -> hasAdminPermissions) // We don't need to include this, but it's safer to
            .refreshOption(
                ctx -> (ctx.id.enabled ? "Disable" : "Enable") + " entry",
                ctx -> {
                    ctx.id.enabled = !ctx.id.enabled;
                    // ex: "Nex instances have been disabled by Mike"
                    PlayerHandler.yell(InstanceId.formatName(ctx.id) + " instances have been " + (ctx.id.enabled ? "enabled" : "disabled") + " by " + TextUtils.getPlayerNameWithCrown(c) + ".");
                }
            )
            .refreshOption(
                ctx -> (ctx.id.invalidateKilltimes ? "Disable" : "Enable") + " kill time registration",
                ctx -> ctx.id.enabled = !ctx.id.invalidateKilltimes
            )
            .refreshOption(
                new MenuLayer.OptionConfig<>(bossOptionsList) // An alternate API to improve readability
                    .label(
                        ctx -> "Disband all " + InstanceId.formatName(ctx.id) + " instances"
                    ).onClick(
                        ctx -> disbandAll(ctx.id, c)
                    ).showIf(
                        ctx -> hasAdminPermissions
                    ).confirmationTitle(
                        ctx -> "Disband all: " + InstanceId.formatName(ctx.id)
                    )
            );

        instanceList
            .pagePermission(ctx -> hasPermissions)
            .selectionMenu(
                (ctx) -> InstanceManager.instances.stream().filter(instance -> instance.boss == ctx.id).toList(),
                (ctx, instance) -> summarizeInstance(instance),
                (ctx, instance) -> instanceList.next(instanceOptions, new InstanceChosenContext(instance))
                // By default, calling layer.next(nextLayer, nextContext) will set the next page title to the label text of the selected label
                // To override this, just call layer.contextTitle(Function<Context, String>) and manually set the title
            );

        instanceOptions
            .pagePermission(ctx -> hasPermissions)
            .refreshOption(
                ctx -> "Spawn",
                ctx -> ctx.instance.reset()
            )
            .refreshOption(
                ctx -> "Despawn",
                ctx -> ctx.instance.despawn()
            )
            .backtrackOption(
                new MenuLayer.OptionConfig<>(instanceOptions)
                    .label(ctx -> "Disband")
                    .onClick(ctx -> disband(ctx.instance, c))
                    .showIf(ctx -> hasPermissions)
                    .confirmationTitle(ctx -> "Disband instance: " + summarizeInstance(ctx.instance)
                )
            )
            .closeOption(
                ctx -> "Join (not participant)",
                ctx -> ctx.instance.quietEnter(c)
            )
            .closeOption(
                ctx -> "Join (particpant)",
                ctx -> ctx.instance.playerEntered(c)
            )
            .forwardOption(
                ctx -> "Members",
                ctx -> instanceOptions.next(membersList)
            );

        membersList
            .pagePermission(ctx -> hasPermissions)
            .selectionMenu(
                (ctx) -> ctx.instance.getValidatedPlayers(),
                (ctx, member) -> member.playerName,
                (ctx, member) -> membersList.next(instanceMemberOptions, new MemberChosenContext(ctx.instance, member))
            );

        instanceMemberOptions
            .pagePermission(ctx -> hasPermissions)
            .closeOption(
                ctx -> "Teleport to",
                ctx -> c.getPA().movePlayer(ctx.member.getCoord())
            )
            .backtrackOption(
                ctx -> "Kick",
                ctx -> {
                    ctx.member.sendMessage(TextUtils.getPlayerNameWithCrown(c) + " removed you from the instance.");
                    removePlayer(ctx.member, ctx.instance);
                },
                ctx -> c.playerRights > ctx.member.playerRights,
                ctx -> "Kick " + ctx.member.playerName
            );

        base.send();
    }

    public static String summarizeInstance(Instance instance){
        return instance.getParticipants().size() + " - " +
            TextUtils.capitalize(instance.type.name()) + " - " +
            (instance.isClanLinked() ? instance.linkedClan.ownerName :
                String.join(", ",
                    instance.getParticipants().stream()
                        .limit(3)
                        .map(info -> info.player.playerName)
                        .toArray(String[]::new)));
    }
}
