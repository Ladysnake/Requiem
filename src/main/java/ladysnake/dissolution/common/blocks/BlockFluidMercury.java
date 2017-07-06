package ladysnake.dissolution.common.blocks;

import java.util.Random;

import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidMercury extends BlockFluidClassic {

	public BlockFluidMercury(Fluid fluid) {
		super(fluid, new MaterialLiquid(MapColor.LIGHT_BLUE));
		this.setDensity(3);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entityIn) {
		
		Random ran = new Random();
	
			if (entityIn instanceof EntityPlayer) {
				
				EntityPlayer player = (EntityPlayer)entityIn;
				
				if(player.getActivePotionEffects().isEmpty()){
					player.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 200, 2));
					
				}
				
				if (IncorporealDataHandler.getHandler(player).isIncorporeal()) {
					
					if(!world.isRemote)
						world.playSound(null, player.getPosition(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, 9.0F, 0.8F + ran.nextFloat() * 0.3F);
					IncorporealDataHandler.getHandler(player).setIncorporeal(false, player);
					
				}
			}
			if (entityIn instanceof EntityLiving && ((EntityLiving) entityIn).getActivePotionEffects().isEmpty()) {
				((EntityLiving) entityIn).addPotionEffect(new PotionEffect(Potion.getPotionById(19), 200, 2));
			}
		

		super.onEntityCollidedWithBlock(world, pos, state, entityIn);
	}

}
