package server.model.npcs.instances;

import server.model.npcs.Coordinate;
import server.model.npcs.instances.nex.NexInstance;
import server.model.npcs.instances.puro_puro.PuroPuroInstance;
import server.world.WorldMap;
import server.world.bounding.WorldArea;

public enum InstanceId {

    NEX(WorldMap.NEX_AREA, WorldMap.NEX_BANK_TELEPORT, NexInstance::new, NexInstance::load),
    PURO_PURO(WorldMap.PURO_PURO, WorldMap.PURO_PURO_TELEPORT, PuroPuroInstance::new, PuroPuroInstance::load),
    ;


    InstanceId(WorldArea worldArea, Coordinate outside, InstanceBuilder builder, Runnable loader){
        this(worldArea, outside, builder, loader, 0);
    }
    /**
     * @param worldArea a world area which can be localized to an instanced area
     * @param outside the coordinate to teleport players to if they log into an instance they shouldn't be in
     *                this is usually the public area next to the portal/other instance creation object
     * @param builder some object or method which will create a new instance from (owner, InstanceType, height) parameters
     * @param onLoad some object or method which will set up private portals and static variables on server start
     * @param heightOffset the offset to use when assigning players to heights and moving them to the instance.
     *                     for a boss whose area is located on height 1, this is 1
     */
    InstanceId(WorldArea worldArea, Coordinate outside, InstanceBuilder builder, Runnable onLoad, int heightOffset){
        this.worldArea = worldArea;
        this.outside = outside;
        this.builder = builder;
        this.loader = onLoad;
        this.heightOffset = heightOffset;
    }

    public final WorldArea worldArea;
    public final Coordinate outside;
    public final InstanceBuilder builder;
    public final Runnable loader;
    public final int heightOffset;
}
