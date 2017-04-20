package ladysnake.tartaros.common.entity;

import ladysnake.tartaros.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityItemWaystone extends EntityItem {

	public EntityItemWaystone(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.setEntityItemStack(new ItemStack(ModBlocks.mercurius_waystone));
	}
	
	@Override
	protected void setOnFireFromLava() {}
	
	@Override
	public void onUpdate() {
		System.out.println("I am a waystone");
		super.onUpdate();
		if (this.onGround)
        {
            world.setBlockState(getPosition(), ModBlocks.mercurius_waystone.getDefaultState() , 11);
            this.setDead();
        }
	}
}
