package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public final class RequiemEntities {
    public static final EntityType<HorologistEntity> HOROLOGIST = FabricEntityTypeBuilder.create(EntityCategory.MISC, HorologistEntity::new)
        .size(EntityDimensions.changing(0.6F, 0.95F))
        .trackable(64, 1, true)
        .build();

    public static void init() {
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("horologist"), HOROLOGIST);
    }

}
