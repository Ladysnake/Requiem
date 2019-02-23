package ladysnake.dissolution.common.impl.possession.entity;

import ladysnake.dissolution.common.entity.DissolutionEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.world.World;

/**
 * Only there to provide <code>super</code> constructors for {@link PossessableEntityImpl}
 */
public abstract class PossessableEntityBase extends MobEntityWithAi {
    protected PossessableEntityBase(World world) {
        this(DissolutionEntities.DEBUG_POSSESSABLE, world);
    }

    protected PossessableEntityBase(EntityType<? extends MobEntityWithAi> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeContainer().register(EntityAttributes.ATTACK_DAMAGE);
    }
}
