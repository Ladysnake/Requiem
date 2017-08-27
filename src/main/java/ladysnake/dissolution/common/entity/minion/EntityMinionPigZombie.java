package ladysnake.dissolution.common.entity.minion;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMinionPigZombie extends EntityMinionZombie {
	
	public EntityMinionPigZombie(World worldIn) {
		this(worldIn, false);
	}

	public EntityMinionPigZombie(World worldIn, boolean isChild) {
		super(worldIn, false, isChild);
	}
	
	@Override
	protected void handleSunExposure() {}
	
	@Override
	protected SoundEvent getAmbientSound()
    {
        return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT;
    }

    protected SoundEvent getHurtSound()
    {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_DEATH;
    }

    protected SoundEvent getStepSound()
    {
    	return (isInert()) ? null : SoundEvents.ENTITY_ZOMBIE_STEP;
    }

}
