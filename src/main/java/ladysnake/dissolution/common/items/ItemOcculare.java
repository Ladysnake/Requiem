package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemOcculare extends Item {

	public ItemOcculare() {
		super();
		this.setMaxStackSize(1);
		this.addPropertyOverride(
				new ResourceLocation(Reference.MOD_ID, "fueled"), 
				(stack, worldIn, entityIn) -> 
				entityIn instanceof EntityPlayer && (!DissolutionInventoryHelper.findItem((EntityPlayer) entityIn, ModItems.SOUL_IN_A_BOTTLE).isEmpty()) 
						? 1.0F
						: 0.0F
		);
		this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "resurrecting"), 
				(stack, worldIn, entityIn) ->
				entityIn == null ? 0.0F
						: (entityIn.getActiveItemStack().getItem() != ModItems.EYE_OF_THE_UNDEAD ? 0.0F
								: (float) (stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F));
		this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "shell"), 
				(stack, worldIn, entityIn) -> {
					ItemStack shell = getShell(stack);
					return shell.isEmpty() ? 0f : ((ItemOccularePart)shell.getItem()).getId();
				});
		this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "etching"), 
				(stack, worldIn, entityIn) -> {
					ItemStack etching = getEtching(stack);
					return etching.isEmpty() ? 0f : ((ItemOccularePart)etching.getItem()).getId();
				});

	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		ItemStack shell = getShell(stack);
		return shell.getItem().getMaxDamage(shell);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		{
			playerIn.setActiveHand(handIn);
			return new ActionResult(EnumActionResult.SUCCESS, itemstack);
		}
	}
	
	public ItemStack getShell(ItemStack stack) {
		NBTTagCompound compound = stack.getSubCompound("shell");
		if(compound != null) {
			ItemStack shell = new ItemStack(compound);
			if(shell.getItem() instanceof ItemOccularePart)
				return shell;
		}
		return new ItemStack(ModItems.IRON_SHELL);
	}
	
	public ItemStack setShell(ItemStack occulare, ItemStack shell) {
		if(shell.getItem() instanceof ItemOccularePart)
			occulare.getTagCompound().setTag("shell", shell.serializeNBT());
		return occulare;
	}
	
	public ItemStack getEtching(ItemStack stack) {
		NBTTagCompound compound = stack.getSubCompound("etching");
		if(compound != null) {
			ItemStack etching = new ItemStack(compound);
			if(etching.getItem() instanceof ItemOccularePart)
				return etching;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack setEtching(ItemStack occulare, ItemStack etching) {
		if(etching.getItem() instanceof ItemOccularePart)
			occulare.getTagCompound().setTag("etching", etching.serializeNBT());
		return occulare;
	}

}

