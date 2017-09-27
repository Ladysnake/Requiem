package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemAcerbacaFruit extends ItemFood {

	public ItemAcerbacaFruit(int amount, float saturation) {
		super(amount, saturation, false);
		this.setAlwaysEdible();
	}
	
	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		super.onFoodEaten(stack, worldIn, player);
		player.getCapability(CapabilityIncorporealHandler.CAPABILITY_INCORPOREAL, null).split();
	}

}
