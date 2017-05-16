package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityItemWaystone extends EntityItem {

	public EntityItemWaystone(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.setEntityItemStack(new ItemStack(ModBlocks.MERCURIUS_WAYSTONE));
	}
	
	@Override
	protected void setOnFireFromLava() {}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.onGround)
        {
            world.setBlockState(getPosition(), ModBlocks.MERCURIUS_WAYSTONE.getDefaultState() , 11);
            ModBlocks.MERCURIUS_WAYSTONE.placeSoulAnchor(world, getPosition(), this);
            this.setDead();
        }
	}
}
