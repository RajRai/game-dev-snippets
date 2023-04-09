package server.model.npcs.instances.puro_puro;

import server.ServerLogger;
import server.model.npcs.Coordinate;
import server.model.npcs.NPCHandler;
import server.model.npcs.instances.*;
import server.model.players.Player;
import server.world.WorldMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

public class PuroPuroInstance extends Instance {

    // Run this if you need to get the Puro-Puro spawn info from the file
    public static void main(String[] args){
        dumpPuroPuroSpawnInfo();
    }

    public PuroPuroInstance(Player owner, InstanceType type, int heightLevel) {
        super(owner, InstanceId.PURO_PURO, type, heightLevel);
    }

    @Override
    public void teleportInside(Player p) {
        Coordinate to = WorldMap.PURO_PURO_TELEPORT;
        p.getPA().startTeleport(to.getX(), to.getY(), super.heightLevel, "puro-puro");
    }

    public static void load(){
        InstanceManager.portals.add(
            PrivatePortal.Builder.create()
                .obX(2588)
                .obY(4316)
                .defaultActionObserver(0, InstanceId.PURO_PURO)
                .build()
        );
    }

    @Override
    public void spawn(boolean firstSpawn) {
        // Dumped from config file
        NPCHandler.newNPC(60063,2616,4344,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4335,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4325,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4315,heightLevel,5);
        NPCHandler.newNPC(60063,2613,4309,heightLevel,5);
        NPCHandler.newNPC(60063,2614,4299,heightLevel,5);
        NPCHandler.newNPC(60063,2614,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2608,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2609,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2617,4315,heightLevel,3);
        NPCHandler.newNPC(60063,2617,4323,heightLevel,3);
        NPCHandler.newNPC(60063,2617,4332,heightLevel,3);
        NPCHandler.newNPC(60063,2616,4337,heightLevel,3);
        NPCHandler.newNPC(60062,2599,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2590,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2579,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2567,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2570,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2582,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2593,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2605,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2595,4295,heightLevel,5);
        NPCHandler.newNPC(1644,2605,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2596,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2583,4300,heightLevel,5);
        NPCHandler.newNPC(1644,2575,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2570,4304,heightLevel,5);
        NPCHandler.newNPC(1644,2570,4308,heightLevel,5);
        NPCHandler.newNPC(1644,2572,4309,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4315,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4307,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4301,heightLevel,5);
        NPCHandler.newNPC(60060,2569,4313,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4321,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4328,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4336,heightLevel,5);
        NPCHandler.newNPC(60060,2572,4341,heightLevel,5);
        NPCHandler.newNPC(60060,2573,4334,heightLevel,5);
        NPCHandler.newNPC(60060,2567,4341,heightLevel,5);
        NPCHandler.newNPC(1643,2573,4324,heightLevel,5);
        NPCHandler.newNPC(1643,2573,4317,heightLevel,5);
        NPCHandler.newNPC(1643,2576,4317,heightLevel,5);
        NPCHandler.newNPC(1643,2576,4310,heightLevel,5);
        NPCHandler.newNPC(1643,2570,4321,heightLevel,5);
        NPCHandler.newNPC(1643,2575,4320,heightLevel,5);
        NPCHandler.newNPC(1643,2567,4333,heightLevel,5);
        NPCHandler.newNPC(1652,2577,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2585,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2593,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2589,4344,heightLevel,5);
        NPCHandler.newNPC(1652,2581,4344,heightLevel,5);
        NPCHandler.newNPC(1652,2572,4344,heightLevel,5);
        NPCHandler.newNPC(1651,2592,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2601,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2610,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4338,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4333,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4328,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4307,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4317,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4327,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4335,heightLevel,5);
        NPCHandler.newNPC(60061,2604,4336,heightLevel,5);
        NPCHandler.newNPC(60061,2598,4336,heightLevel,5);
        NPCHandler.newNPC(1650,2605,4332,heightLevel,5);
        NPCHandler.newNPC(1650,2598,4332,heightLevel,5);
        NPCHandler.newNPC(1650,2590,4333,heightLevel,5);
        NPCHandler.newNPC(1650,2591,4330,heightLevel,5);
        NPCHandler.newNPC(1650,2585,4330,heightLevel,5);
        NPCHandler.newNPC(1650,2583,4332,heightLevel,5);
        NPCHandler.newNPC(1649,2578,4326,heightLevel,5);
        NPCHandler.newNPC(1649,2579,4321,heightLevel,5);
        NPCHandler.newNPC(1649,2581,4316,heightLevel,5);
        NPCHandler.newNPC(1649,2581,4310,heightLevel,5);
        NPCHandler.newNPC(1649,2583,4307,heightLevel,5);
        NPCHandler.newNPC(1649,2587,4307,heightLevel,5);
        NPCHandler.newNPC(1648,2589,4304,heightLevel,5);
        NPCHandler.newNPC(1648,2595,4303,heightLevel,5);
        NPCHandler.newNPC(1648,2598,4306,heightLevel,5);
        NPCHandler.newNPC(1648,2602,4304,heightLevel,5);
        NPCHandler.newNPC(1648,2605,4308,heightLevel,5);
        NPCHandler.newNPC(1648,2608,4311,heightLevel,5);
        NPCHandler.newNPC(1647,2605,4316,heightLevel,5);
        NPCHandler.newNPC(1647,2605,4323,heightLevel,5);
        NPCHandler.newNPC(1647,2604,4317,heightLevel,5);
        NPCHandler.newNPC(1647,2602,4310,heightLevel,5);
        NPCHandler.newNPC(1647,2597,4309,heightLevel,5);
        NPCHandler.newNPC(1647,2597,4307,heightLevel,5);
        NPCHandler.newNPC(1646,2581,4314,heightLevel,5);
        NPCHandler.newNPC(1646,2582,4319,heightLevel,5);
        NPCHandler.newNPC(1646,2581,4328,heightLevel,5);
        NPCHandler.newNPC(1646,2586,4329,heightLevel,5);
        NPCHandler.newNPC(1646,2590,4327,heightLevel,5);
        NPCHandler.newNPC(1646,2598,4327,heightLevel,5);
        NPCHandler.newNPC(1645,2599,4321,heightLevel,5);
        NPCHandler.newNPC(1645,2599,4315,heightLevel,5);
        NPCHandler.newNPC(1645,2595,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2589,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2584,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2595,4327,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4344,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4335,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4325,heightLevel,5);
        NPCHandler.newNPC(60063,2616,4315,heightLevel,5);
        NPCHandler.newNPC(60063,2613,4309,heightLevel,5);
        NPCHandler.newNPC(60063,2614,4299,heightLevel,5);
        NPCHandler.newNPC(60063,2614,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2608,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2609,4295,heightLevel,5);
        NPCHandler.newNPC(60063,2617,4315,heightLevel,3);
        NPCHandler.newNPC(60063,2617,4323,heightLevel,3);
        NPCHandler.newNPC(60063,2617,4332,heightLevel,3);
        NPCHandler.newNPC(60063,2616,4337,heightLevel,3);
        NPCHandler.newNPC(60062,2599,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2590,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2579,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2567,4295,heightLevel,5);
        NPCHandler.newNPC(60062,2570,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2582,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2593,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2605,4298,heightLevel,5);
        NPCHandler.newNPC(60062,2595,4295,heightLevel,5);
        NPCHandler.newNPC(1644,2605,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2596,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2583,4300,heightLevel,5);
        NPCHandler.newNPC(1644,2575,4301,heightLevel,5);
        NPCHandler.newNPC(1644,2570,4304,heightLevel,5);
        NPCHandler.newNPC(1644,2570,4308,heightLevel,5);
        NPCHandler.newNPC(1644,2572,4309,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4315,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4307,heightLevel,5);
        NPCHandler.newNPC(1644,2567,4301,heightLevel,5);
        NPCHandler.newNPC(60060,2569,4313,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4321,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4328,heightLevel,5);
        NPCHandler.newNPC(60060,2570,4336,heightLevel,5);
        NPCHandler.newNPC(60060,2572,4341,heightLevel,5);
        NPCHandler.newNPC(60060,2573,4334,heightLevel,5);
        NPCHandler.newNPC(60060,2567,4341,heightLevel,5);
        NPCHandler.newNPC(1643,2573,4324,heightLevel,5);
        NPCHandler.newNPC(1643,2573,4317,heightLevel,5);
        NPCHandler.newNPC(1643,2576,4317,heightLevel,5);
        NPCHandler.newNPC(1643,2576,4310,heightLevel,5);
        NPCHandler.newNPC(1643,2570,4321,heightLevel,5);
        NPCHandler.newNPC(1643,2575,4320,heightLevel,5);
        NPCHandler.newNPC(1643,2567,4333,heightLevel,5);
        NPCHandler.newNPC(1652,2577,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2585,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2593,4341,heightLevel,5);
        NPCHandler.newNPC(1652,2589,4344,heightLevel,5);
        NPCHandler.newNPC(1652,2581,4344,heightLevel,5);
        NPCHandler.newNPC(1652,2572,4344,heightLevel,5);
        NPCHandler.newNPC(1651,2592,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2601,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2610,4341,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4338,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4333,heightLevel,5);
        NPCHandler.newNPC(1651,2611,4328,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4307,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4317,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4327,heightLevel,5);
        NPCHandler.newNPC(60061,2608,4335,heightLevel,5);
        NPCHandler.newNPC(60061,2604,4336,heightLevel,5);
        NPCHandler.newNPC(60061,2598,4336,heightLevel,5);
        NPCHandler.newNPC(1650,2605,4332,heightLevel,5);
        NPCHandler.newNPC(1650,2598,4332,heightLevel,5);
        NPCHandler.newNPC(1650,2590,4333,heightLevel,5);
        NPCHandler.newNPC(1650,2591,4330,heightLevel,5);
        NPCHandler.newNPC(1650,2585,4330,heightLevel,5);
        NPCHandler.newNPC(1650,2583,4332,heightLevel,5);
        NPCHandler.newNPC(1649,2578,4326,heightLevel,5);
        NPCHandler.newNPC(1649,2579,4321,heightLevel,5);
        NPCHandler.newNPC(1649,2581,4316,heightLevel,5);
        NPCHandler.newNPC(1649,2581,4310,heightLevel,5);
        NPCHandler.newNPC(1649,2583,4307,heightLevel,5);
        NPCHandler.newNPC(1649,2587,4307,heightLevel,5);
        NPCHandler.newNPC(1648,2589,4304,heightLevel,5);
        NPCHandler.newNPC(1648,2595,4303,heightLevel,5);
        NPCHandler.newNPC(1648,2598,4306,heightLevel,5);
        NPCHandler.newNPC(1648,2602,4304,heightLevel,5);
        NPCHandler.newNPC(1648,2605,4308,heightLevel,5);
        NPCHandler.newNPC(1648,2608,4311,heightLevel,5);
        NPCHandler.newNPC(1647,2605,4316,heightLevel,5);
        NPCHandler.newNPC(1647,2605,4323,heightLevel,5);
        NPCHandler.newNPC(1647,2604,4317,heightLevel,5);
        NPCHandler.newNPC(1647,2602,4310,heightLevel,5);
        NPCHandler.newNPC(1647,2597,4309,heightLevel,5);
        NPCHandler.newNPC(1647,2597,4307,heightLevel,5);
        NPCHandler.newNPC(1646,2581,4314,heightLevel,5);
        NPCHandler.newNPC(1646,2582,4319,heightLevel,5);
        NPCHandler.newNPC(1646,2581,4328,heightLevel,5);
        NPCHandler.newNPC(1646,2586,4329,heightLevel,5);
        NPCHandler.newNPC(1646,2590,4327,heightLevel,5);
        NPCHandler.newNPC(1646,2598,4327,heightLevel,5);
        NPCHandler.newNPC(1645,2599,4321,heightLevel,5);
        NPCHandler.newNPC(1645,2599,4315,heightLevel,5);
        NPCHandler.newNPC(1645,2595,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2589,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2584,4312,heightLevel,5);
        NPCHandler.newNPC(1645,2595,4327,heightLevel,5);
    }
}
