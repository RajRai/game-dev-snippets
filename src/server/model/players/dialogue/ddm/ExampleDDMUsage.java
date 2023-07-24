package server.model.players.dialogue.ddm;

import server.Server;
import server.model.npcs.instances.Instance;
import server.model.npcs.instances.InstanceId;
import server.model.npcs.instances.InstanceManager;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.PlayerRights;
import server.util.TextUtils;

import java.util.Arrays;

public class ExampleDDMUsage {

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
                        ctx -> InstanceManager.disbandAll(ctx.id, c)
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
                (ctx, instance) -> InstanceManager.summarizeInstance(instance),
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
                    .onClick(ctx -> InstanceManager.disband(ctx.instance, c))
                    .showIf(ctx -> hasPermissions)
                    .confirmationTitle(ctx -> "Disband instance: " + InstanceManager.summarizeInstance(ctx.instance)
                )
            )
            .closeOption(
                ctx -> "Join (not participant)",
                ctx -> ctx.instance.quietEnter(c)
            )
            .closeOption(
                ctx -> "Join (participant)",
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
                    InstanceManager.removePlayer(ctx.member, ctx.instance);
                },
                ctx -> c.playerRights > ctx.member.playerRights,
                ctx -> "Kick " + ctx.member.playerName
            );

        base.send();
    }
}
