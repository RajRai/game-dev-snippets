package server.broadcasts;

import server.Server;
import server.dao.database.WorkerJobs;
import server.job.DeleteBroadcast;
import server.job.LoadBroadcasts;
import server.job.UpdateBroadcast;
import server.model.events.GameEvent;
import server.model.events.GameEvents;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.model.players.dialogue.ddm.MenuLayer;
import server.util.Misc;
import server.util.TextUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Broadcasts {

    private static final HashSet<Broadcast> drafts = new HashSet<>();
    private static final ArrayList<Broadcast> active = new ArrayList<>();

    private static Broadcast current = null;

    public static void process(){
        if (current != null){
            if (current.endTime() > Broadcast.NEXT_SERVER_RESTART && Server.currentTime > current.endTime() || !active.contains(current)){
                active.remove(current);
                current = null;
            } else {
                return;
            }
        }
        active.removeAll(
            active.stream()
                .filter(broadcast -> broadcast.endTime() > Broadcast.NEXT_SERVER_RESTART && broadcast.endTime() < Server.currentTime)
                .peek(broadcast -> WorkerJobs.getInstance().addJob(new DeleteBroadcast(broadcast.broadcastId())))
                .toList()
        );
        for (Broadcast broadcast : active) {
            if (broadcast.startTime() > Broadcast.NEXT_SERVER_RESTART && broadcast.startTime() < Server.currentTime){
                current = broadcast;
                Arrays.stream(PlayerHandler.players)
                    .filter(Objects::nonNull)
                    .forEach(p -> p.getPA().sendBroadcast(broadcast));
                return;
            }
        }
    }

    public static void addActive(Broadcast broadcast) {
        active.add(broadcast);
    }

    public static boolean isActive(Broadcast broadcast) {
        return active.contains(broadcast);
    }

    public static void addDraft(Broadcast broadcast) {
        drafts.add(broadcast);
    }

    public static void sendCurrent(Player c) {
        if (c == null){
            return;
        }
        c.getPA().sendBroadcast(current);
    }

    /*
     * Dialogue
     */

    private enum InputType {
        FOR_NEW,
        FOR_MODIFY
    }

    private enum DateType {
        START_DATE("start"),
        END_DATE("end")
        ;

        private final String string;
        DateType(String string){
            this.string = string;
        }
    }

    private record InputNeeded(Broadcast broadcast, InputType type){}
    private record DateNeeded(Broadcast broadcast, InputType inputType, DateType dateType){}
    private record EventChosenForSync(GameEvent event, Broadcast broadcast, InputType inputType, DateType dateType){}

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public static void showDialogue(Player c){
        MenuLayer<Player> chooseAction = new MenuLayer<>(c);
        MenuLayer<Player> chooseActiveBroadcast = new MenuLayer<>(c);
        MenuLayer<Player> chooseDraftBroadcast = new MenuLayer<>(c);
        MenuLayer<InputNeeded> getMessage = new MenuLayer<>(c);
        MenuLayer<InputNeeded> getURL = new MenuLayer<>(c);
        MenuLayer<DateNeeded> chooseDate = new MenuLayer<>(c);
        MenuLayer<DateNeeded> chooseServerEvent = new MenuLayer<>(c);
        MenuLayer<EventChosenForSync> chooseEventSyncType = new MenuLayer<>(c);
        MenuLayer<DateNeeded> manualInputDate = new MenuLayer<>(c);
        MenuLayer<Broadcast> modifyBroadcast = new MenuLayer<>(c);

        AtomicReference<StringBuilder> messageBuilder = new AtomicReference<>(new StringBuilder());
        AtomicReference<StringBuilder> urlBuilder = new AtomicReference<>(new StringBuilder());

        chooseAction
            .forwardOption(
                ctx -> "Modify activated broadcast",
                ctx -> chooseAction.next(chooseActiveBroadcast)
            )
            .forwardOption(
                ctx -> "Modify drafted broadcast",
                ctx -> chooseAction.next(chooseDraftBroadcast)
            )
            .forwardOption(
                ctx -> "Make new broadcast",
                ctx -> {
                    Broadcast b = new Broadcast();
                    drafts.add(b);
                    return chooseAction.next(getMessage, new InputNeeded(b, InputType.FOR_NEW));
                }
            );

        getMessage
            .inputPage(
                ctx -> messageBuilder.get().isEmpty() ? "Enter broadcast message" : "Continue broadcast message (blank to finish)",
                (ctx, input) -> {
                    if (input.isEmpty() || input.isBlank()){
                        ctx.broadcast.message(messageBuilder.toString());
                        messageBuilder.set(new StringBuilder());
                        return switch(ctx.type) {
                            case FOR_NEW -> getMessage.next(getURL);
                            case FOR_MODIFY -> getMessage.next(modifyBroadcast, ctx.broadcast);
                        };
                    }
                    messageBuilder.get().append(input).append(" ");
                    return getMessage.next(getMessage);
                }
            );

        getURL
            .inputPage(
                ctx -> urlBuilder.get().isEmpty() ? "Enter broadcast URL (blank for none)" : "Continue URL (blank to finish)",
                (ctx, input) -> {
                    if (input.isEmpty() || input.isBlank()){
                        ctx.broadcast.url(urlBuilder.toString());
                        urlBuilder.set(new StringBuilder());
                        return switch(ctx.type) {
                            case FOR_NEW -> getURL.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.type, DateType.START_DATE));
                            case FOR_MODIFY -> getURL.next(modifyBroadcast, ctx.broadcast);
                        };
                    }
                    urlBuilder.get().append(input);
                    return getURL.next(getURL);
                }
            );

        // todo: allow offsetting from the other data using Duration.parse
        chooseDate
            // "Choose start/end date:"
            .contextTitle(ctx -> "Choose " + ctx.dateType.string + " date:")
            .forwardOption(
                ctx -> "Next server restart",
                ctx -> {
                    if (ctx.dateType == DateType.END_DATE){
                        ctx.broadcast.endTime(Broadcast.NEXT_SERVER_RESTART);
                    } else {
                        ctx.broadcast.startTime(Broadcast.NEXT_SERVER_RESTART);
                    }
                    if (ctx.inputType == InputType.FOR_NEW && ctx.dateType == DateType.START_DATE){
                        return chooseDate.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.inputType, DateType.END_DATE));
                    }
                    return chooseDate.next(modifyBroadcast, ctx.broadcast);
                }
            )
            .forwardOption(
                ctx -> "Sync with event",
                ctx -> chooseDate.next(chooseServerEvent)
            )
            .forwardOption(
                ctx -> "Enter manually",
                ctx -> chooseDate.next(manualInputDate)
            )
            .forwardOption(
                ctx -> "Schedule now",
                ctx -> {
                    ctx.broadcast.startTime(Server.currentTime);
                    return chooseDate.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.inputType, DateType.END_DATE));
                },
                ctx -> ctx.dateType == DateType.START_DATE
            );

        chooseServerEvent
            .selectionMenu(
                ctx -> GameEvents.getScheduledEvents(),
                (ctx, event) -> event.getEventName(),
                (ctx, event) -> chooseServerEvent.next(chooseEventSyncType, new EventChosenForSync(event, ctx.broadcast, ctx.inputType, ctx.dateType))
            );

        chooseEventSyncType
            .forwardOption(
                // "start/end broadcast on event start"
                ctx -> TextUtils.capitalize(ctx.dateType.string) + " broadcast on event start",
                ctx -> {
                    if (ctx.dateType == DateType.END_DATE){
                        ctx.broadcast.endTime(ctx.event.getEventStartTime());
                    } else {
                        ctx.broadcast.startTime(ctx.event.getEventStartTime());
                    }
                    if (ctx.inputType == InputType.FOR_NEW && ctx.dateType == DateType.START_DATE){
                        return chooseServerEvent.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.inputType, DateType.END_DATE));
                    }
                    return chooseServerEvent.next(modifyBroadcast, ctx.broadcast);
                }
            )
            .forwardOption(
                // "start/end broadcast on event end"
                ctx -> TextUtils.capitalize(ctx.dateType.string) + " broadcast on event end",
                ctx -> {
                    if (ctx.dateType == DateType.END_DATE){
                        ctx.broadcast.endTime(ctx.event.getEventEndTime());
                    } else {
                        ctx.broadcast.startTime(ctx.event.getEventEndTime());
                    }
                    if (ctx.inputType == InputType.FOR_NEW && ctx.dateType == DateType.START_DATE){
                        return chooseServerEvent.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.inputType, DateType.END_DATE));
                    }
                    return chooseServerEvent.next(modifyBroadcast, ctx.broadcast);
                }
            );

        manualInputDate
            .inputPage(
                ctx -> "Enter date in YYYY/MM/DD HH:MM format (UTC)",
                (ctx, input) -> {
                    String userInput = input.trim();
                    try {
                        LocalDateTime parsedDateTime = LocalDateTime.parse(userInput, dtf);
                        ctx.broadcast.endTime(Misc.clampLongToTimestamp(parsedDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
                    } catch (DateTimeParseException e) {
                        c.getPA().closeAllWindows();
                        c.getDH().build()
                            .sendStatement("Not formatted correctly")
                            .sendStatement("Please wait...", manualInputDate::send)
                            .run();
                    }
                    if (ctx.inputType == InputType.FOR_NEW && ctx.dateType == DateType.START_DATE){
                        return manualInputDate.next(chooseDate, new DateNeeded(ctx.broadcast, ctx.inputType, DateType.END_DATE));
                    }
                    return manualInputDate.next(modifyBroadcast, ctx.broadcast);
                }
            );

        chooseActiveBroadcast
            .selectionMenu(
                ctx -> active,
                (ctx, bc) -> bc.message(),
                (ctx, bc) -> chooseDraftBroadcast.next(modifyBroadcast, bc)
            );

        chooseDraftBroadcast
            .selectionMenu(
                ctx -> drafts,
                (ctx, bc) -> bc.message(),
                (ctx, bc) -> chooseDraftBroadcast.next(modifyBroadcast, bc)
            );

        modifyBroadcast
            .refreshOption(
                new MenuLayer.OptionConfig<>(modifyBroadcast)
                    .label(ctx -> active.contains(ctx) ? "Deactivate" : "Make active")
                    .onClick(ctx -> {
                        if (!active.contains(ctx)) {
                            drafts.remove(ctx);
                            active.add(ctx);
                        } else {
                            drafts.add(ctx);
                            active.remove(ctx);
                        }
                        WorkerJobs.getInstance().addJob(new UpdateBroadcast(ctx));
                    })
                    .confirmationTitle(ctx -> (active.contains(ctx) ? "Deactivate" : "Make active") + ": Are you sure?")
            )
            .backtrackOption(
                ctx -> "Delete draft",
                ctx -> {
                    drafts.remove(ctx);
                    WorkerJobs.getInstance().addJob(new DeleteBroadcast(ctx.broadcastId()));
                },
                ctx -> !active.contains(ctx),
                ctx -> "Delete Draft: Are you sure?"
            )
            .refreshOption(
                ctx -> "Preview",
                ctx -> c.getPA().sendBroadcast(ctx)
            )
            .forwardOption(
                ctx -> "Modify text",
                ctx -> modifyBroadcast.next(getMessage, new InputNeeded(ctx, InputType.FOR_MODIFY)),
                ctx -> !active.contains(ctx) // Don't allow modifying actives
            )
            .forwardOption(
                ctx -> "Modify URL",
                ctx -> modifyBroadcast.next(getURL, new InputNeeded(ctx, InputType.FOR_MODIFY)),
                ctx -> !active.contains(ctx) // Don't allow modifying actives
            )
            .forwardOption(
                ctx -> {
                    try {
                        String end = ctx.startTime() == Broadcast.NEXT_SERVER_RESTART ? "Next server restart" : dtf.format(Instant.ofEpochMilli(ctx.startTime()).atOffset(ZoneOffset.UTC));
                        return "Modify start date (" + end + ")";
                    } catch (Exception e){
                        return "Error!";
                    }
                },
                ctx -> modifyBroadcast.next(chooseDate, new DateNeeded(ctx, InputType.FOR_MODIFY, DateType.START_DATE)),
                ctx -> !active.contains(ctx) // Don't allow modifying actives
            )
            .forwardOption(
                ctx -> {
                    try {
                        String end = ctx.endTime() == Broadcast.NEXT_SERVER_RESTART ? "Next server restart" : dtf.format(Instant.ofEpochMilli(ctx.endTime()).atOffset(ZoneOffset.UTC));
                        return "Modify end date (" + end + ")";
                    } catch (Exception e){
                        return "Error!";
                    }
                },
                ctx -> modifyBroadcast.next(chooseDate, new DateNeeded(ctx, InputType.FOR_MODIFY, DateType.END_DATE)),
                ctx -> !active.contains(ctx) // Don't allow modifying actives
            );

        chooseAction.withContext(c).send();
    }

    /**
     * For DB reads
     */
    public static void clear(){
        drafts.clear();
        active.clear();
    }

    static {
        WorkerJobs.getInstance().addJob(new LoadBroadcasts(true));
    }
}
