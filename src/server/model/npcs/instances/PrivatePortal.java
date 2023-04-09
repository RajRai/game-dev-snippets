package server.model.npcs.instances;

import server.model.objects.ObjectId;
import server.model.players.ChallengeRequest;
import server.model.players.Player;
import server.model.players.PlayerHandler;
import server.observers.Observers;
import server.observers.players.ActionObserver;

/**
 * To place a basic private portal in the world, call PrivatePortal.Builder.create().build(). Usually in a static block.
 * Object configuration should happen between create() and build(). I.e, Builder.create().obX(9999).obY(8888).build()
 * Private portals don't need to be private portal objects. They can be any game object serving as a private portal,
 * like a door, or a barrier.
 * 
 * If you're reusing an existing game object, set face to -1 so that the object isn't placed. This is done automatically 
 * whenever you use the obId(id) method, or if you call doNotPlace()
 *
 * See PuroPuroInstance (and the Builder functions it calls) for a basic example.
 */
public class PrivatePortal {

    public static final int PORTAL_ID = ObjectId.PRIVATE_PORTAL_9368;

    public final int x;
    public final int y;
    public final int heightOffset;
    public final int obId;
    public final int obFace;
    public final int obType;
    public final boolean walkable;
    public final ActionObserver actionObserver;

    private PrivatePortal(Builder state){
        this.x = state.x;
        this.y = state.y;
        this.heightOffset = state.heightOffset;
        this.obId = state.obId;
        this.obFace = state.obFace;
        this.obType = state.obType;
        this.walkable = state.walkable;
        this.actionObserver = state.actionObserver;

        InstanceManager.portals.add(this);
        if (actionObserver != null){
            Observers.actionObservers.add(actionObserver);
        }
    }

    public boolean shouldPlaceAt(int heightLevel){
        return this.obFace >= 0 && this.heightOffset == heightLevel % 4;
    }


    /**
     * Builder
     */
    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int heightOffset = 0;
        private int obId = PORTAL_ID;
        private int obFace = 0; // -1 means "do not place the object", otherwise, place it with this face
        private int obType = 10;
        private boolean walkable = false;
        private ActionObserver actionObserver = null;

        private Builder(){}

        public static Builder create(){
            return new Builder();
        }

        public PrivatePortal build(){
            return new PrivatePortal(this);
        }

        public Builder obId(int obId) {
            this.obId = obId;
            doNotPlace();
            return this;
        }

        public Builder obX(int x) {
            this.x = Math.max(0, x);
            return this;
        }

        public Builder obY(int y) {
            this.y = Math.max(0, y);
            return this;
        }

        public Builder atHeightOffset(int heightOffset) {
            this.heightOffset = heightOffset % 4;
            return this;
        }

        public Builder placeObject(int face) {
            this.obFace = face;
            return this;
        }

        public Builder doNotPlace(){
            this.obFace = -1;
            return this;
        }

        public Builder obType(int value){
            this.obType = value;
            return this;
        }

        public Builder walkable(boolean value){
            this.walkable = value;
            return this;
        }

        public Builder actionObserver(ActionObserver observer){
            this.actionObserver = observer;
            return this;
        }

        public Builder defaultActionObserver(int menuEntryIndex, InstanceId boss){
            actionObserver = new ActionObserver() {
                @Override
                public String getName() {
                    return "PrivatePortalObserver: " + boss.name();
                }

                @Override
                public boolean onObjectOption(Player c, int obId, int obX, int obY, int obFace, int entry) {
                    if (obId == Builder.this.obId && obX == x && obY == y){
                        if (entry == menuEntryIndex){
                            Instance instance = InstanceManager.getInstance(c);
                            if (instance != null){
                                c.getDH().build()
                                    .sendTitledOptions(
                                        "Private instance options",
                                        "Join", () -> {
                                            if (instance.playerCanEnter(c)){
                                                instance.playerEntered(c);
                                            } else {
                                                c.sendMessage("You can't join the instance right now.");
                                            }
                                        },
                                        "Leave the group", () -> {
                                            if (instance.getOwnerId() == c.getPlayerId() && !instance.canOwnerTransfer()){
                                                c.getDH().build().sendOptions(
                                                    "@dre@Warning: you are the leader. The instance", null,
                                                    "@dre@will be disbanded", null,
                                                    "I'm sure.", () -> {
                                                        InstanceManager.removeInstance(instance);
                                                        c.getPA().closeAllWindows();
                                                    },
                                                    "Never mind.", () -> c.getPA().closeAllWindows()
                                                ).run();
                                            } else {
                                                InstanceManager.removePlayer(c, instance);
                                            }
                                        },
                                        "Never mind.", () -> c.getPA().closeAllWindows()
                                    ).run();
                            } else {
                                c.getDH().build()
                                    .sendTitledOptions(
                                        "Private instance options",
                                        "Create an instance", () -> {
                                            InstanceManager.createInstance(c, boss, InstanceType.PRIVATE);
                                            c.sendMessage("You created a private instance.");
                                            c.getPA().closeAllWindows();
                                        },
                                        "Join an instance", () -> {
                                            c.getDH().sendInputEvent("Player name (must be on their friends list): ", (response) -> {
                                                Player p = PlayerHandler.getPlayer(response);
                                                if (p == null || !p.getPA().isInFriends(c)){
                                                    c.sendMessage("Couldn't find a player with that name.");
                                                    c.getPA().closeAllWindows();
                                                    return;
                                                }
                                                Instance joiningInstance = InstanceManager.getInstance(p);
                                                if (joiningInstance == null || joiningInstance.getInstanceId() != boss){
                                                    c.sendMessage("That player doesn't have an instance at this area.");
                                                    c.getPA().closeAllWindows();
                                                    return;
                                                }
                                                new ChallengeRequest(c, p, "requests to join your instance.", (request -> {
                                                    Instance check = InstanceManager.getInstance(p);
                                                    if (check == joiningInstance){
                                                        joiningInstance.addParticipant(c);
                                                    }
                                                }));
                                            });
                                        },
                                        "Never mind.", () -> c.getPA().closeAllWindows()
                                    ).run();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            };
            return this;
        }
    }

}
