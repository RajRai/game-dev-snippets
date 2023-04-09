package server.model.npcs.instances.tombs_of_amascut_WIP;

import server.model.players.Player;

public abstract class TombsPath {

    public final TombsRoom puzzle;
    public final TombsRoom boss;
    public final TombsInstance instance;


    protected TombsPath(TombsRoom puzzle, TombsRoom boss, TombsInstance instance) {
        this.puzzle = puzzle;
        this.boss = boss;
        this.instance = instance;
    }

    protected void enter(Player c){
        puzzle.enter(c);
        instance.currentPath = this;
        if (!puzzle.isFinished()){
            instance.currentRoom = puzzle;
        } else {
            instance.currentRoom = boss;
        }
    }

    protected boolean isFinished(){
        return puzzle.isFinished() && boss.isFinished();
    }

    protected abstract void spawnOsmumten();

    public abstract boolean isTeleportCrystal(int obId);

    public abstract void enterFight(Player c);

    public abstract boolean isPuzzleExit(int obId);
}
