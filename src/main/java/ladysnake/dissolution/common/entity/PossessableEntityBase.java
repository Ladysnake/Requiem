package ladysnake.dissolution.common.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.world.World;

public abstract class PossessableEntityBase extends MobEntityWithAi {
    protected PossessableEntityBase(World world) {
        this(DissolutionEntities.DEBUG_POSSESSABLE, world);
    }

    protected PossessableEntityBase(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }
}
