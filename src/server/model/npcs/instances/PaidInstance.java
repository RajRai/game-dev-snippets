package server.model.npcs.instances;

import server.model.players.Player;
import server.util.TextUtils;
import server.util.TimedEvent;

// todo: add clan coffer
// todo: add clan linking action observer and menu entry to default PrivatePortals.
// todo: add clan permissions for linking instances
public abstract class PaidInstance extends Instance {

    protected boolean ownerEntered = false;
    protected int pricePerHour;
    protected TimedEvent payEvent = new TimedEvent("InstancePayEvent") {
        @Override
        public void fire() {
            // todo: withdraw from clan coffer if the instance is linked to a clan, owners bank, or owners inv, in that priority order, every minute
            schedule(60000);
        }
    };

    public PaidInstance(Player owner, InstanceId boss, InstanceType type, int heightLevel, int pricePerHour) {
        super(owner, boss, type, heightLevel);
        this.pricePerHour = pricePerHour;
    }

    public PaidInstance(Player owner, InstanceId boss, InstanceType type, int heightLevel, int respawnTime, int pricePerHour) {
        super(owner, boss, type, heightLevel, respawnTime);
        this.pricePerHour = pricePerHour;
    }

    @Override
    public boolean canOwnerTransfer() {
        return isClanLinked();
    }

    @Override
    public void playerEntered(Player p) {
        if (p == getOwner() && !ownerEntered){
            p.getDH().build().sendOptions(
                "@dre@Warning: You're creating a paid instance.", null,
                "@dre@This will cost " + TextUtils.format(pricePerHour / 60) + " GP per minute.", null,
                "@dre@The clan coffer will " + (isClanLinked() ? "" : "not ") + "be used.", null,
                "Ok.", () -> {
                    ownerEntered = true;
                    super.playerEntered(p);
                },
                "Never mind.", () -> p.getPA().closeAllWindows()
            );
        } else if (p != getOwner() && !ownerEntered){
            p.sendMessage("You must wait for the owner to enter the instance");
            p.getPA().closeAllWindows();
        } else {
            if (!payEvent.isScheduled()){
                payEvent.schedule(60000);
            }
            super.playerEntered(p);
        }
    }
}
