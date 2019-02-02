package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.Dissolution;
import ladysnake.reflectivefabric.misc.DebugUtil;
import net.fabricmc.fabric.entity.EntityTrackingRegistry;
import net.fabricmc.fabric.entity.FabricEntityTypeBuilder;
import net.minecraft.client.network.packet.MobSpawnClientPacket;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.registry.Registry;

public class DissolutionEntities {
    public static EntityType<PossessableEntityImpl> DEBUG_POSSESSABLE;

    public static void init() {
        if (DebugUtil.isDevEnv()) {
            DEBUG_POSSESSABLE = Registry.register(Registry.ENTITY_TYPE, Dissolution.id("debug_possessable"), FabricEntityTypeBuilder.create(PossessableEntityImpl.class, PossessableEntityImpl::new).size(0.6F, 1.95F).trackable(64, 1, true).build());
            EntityTrackingRegistry.INSTANCE.registerSpawnPacketProvider(DEBUG_POSSESSABLE, e -> new MobSpawnClientPacket((LivingEntity) e));
            EntityTrackingRegistry.INSTANCE.register(EntityType.ZOMBIE, 80, 3, true);
            EntityTrackingRegistry.INSTANCE.registerSpawnPacketProvider(EntityType.ZOMBIE, e -> new MobSpawnClientPacket((LivingEntity) e));
        }
    }
}
