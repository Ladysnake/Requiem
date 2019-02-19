package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.common.impl.possession.entity.PossessableEntityImpl;
import ladysnake.reflectivefabric.misc.DebugUtil;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class DissolutionEntities {
    public static EntityType<PossessableEntityImpl> DEBUG_POSSESSABLE;
    public static EntityType<PlayerShellEntity> PLAYER_SHELL;

    public static void init() {
        if (DebugUtil.isDevEnv()) {
            DEBUG_POSSESSABLE = Registry.register(
                    Registry.ENTITY_TYPE,
                    Dissolution.id("debug_possessable"),
                    FabricEntityTypeBuilder.create(EntityCategory.MISC, PossessableEntityImpl::new)
                            .size(0.6F, 1.95F)
                            .trackable(64, 1, true)
                            .build()
            );
        }
        PLAYER_SHELL = Registry.register(
                Registry.ENTITY_TYPE,
                Dissolution.id("player_shell"),
                FabricEntityTypeBuilder.create(EntityCategory.MISC, PlayerShellEntity::new)
                        .size(EntityType.PLAYER.getWidth(), EntityType.PLAYER.getHeight())
                        .trackable(64, 1, true)
                        .build()
        );
    }
}
