package ladysnake.dissolution.common.entity;

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
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
	}
	
	@Override
	protected void handleSunExposition() {}
	
	@Override
	protected SoundEvent getAmbientSound()
    {
        return (isCorpse()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT;
    }

    protected SoundEvent getHurtSound()
    {
    	return (isCorpse()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return (isCorpse()) ? null : SoundEvents.ENTITY_ZOMBIE_PIG_DEATH;
    }

    protected SoundEvent getStepSound()
    {
    	return (isCorpse()) ? null : SoundEvents.ENTITY_ZOMBIE_STEP;
    }

}
