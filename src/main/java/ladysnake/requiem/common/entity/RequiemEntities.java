package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class RequiemEntities {
    public static EntityType<PlayerShellEntity> PLAYER_SHELL;

    public static void init() {
        PLAYER_SHELL = Registry.register(
                Registry.ENTITY_TYPE,
                Requiem.id("player_shell"),
                FabricEntityTypeBuilder.create(EntityCategory.MISC, PlayerShellEntity::new)
                        .size(EntitySize.resizeable(EntityType.PLAYER.getWidth(), EntityType.PLAYER.getHeight()))
                        .trackable(64, 1, true)
                        .build()
        );
    }
}
