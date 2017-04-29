package ladysnake.tartaros.common.items;

import javax.annotation.Nullable;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.entity.EntityMinion;
import ladysnake.tartaros.common.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEyeDead extends Item {

	public ItemEyeDead() {
		super();
		this.setUnlocalizedName(Reference.Items.EYE_DEAD.getUnlocalizedName());
        this.setRegistryName(Reference.Items.EYE_DEAD.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
        this.setMaxStackSize(1);
        this.setMaxDamage(50);
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID + ":charged"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
//            	System.out.println(entityIn != null && (!findSoul((EntityPlayer)entityIn).isEmpty()) ? 1.0F : 0.0F);
            	return entityIn != null && (!findSoul((EntityPlayer)entityIn).isEmpty()) ? 1.0F : 0.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID + ":resurrecting"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (!(entityLiving instanceof EntityPlayer) || this.getMaxItemUseDuration(stack) - timeLeft < 50) return;
		EntityPlayer player = (EntityPlayer)entityLiving;
		if (IncorporealDataHandler.getHandler(player).isIncorporeal()) return;
		ItemStack ammo = this.findSoul(player);
		ammo.shrink(1);
		if (!worldIn.isRemote) {
			EntityMinion minion = new EntityMinion(worldIn);
			minion.setPosition(player.posX, player.posY, player.posZ);
			worldIn.spawnEntity(minion);	//TODO create a minion entity with custom AI
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		// TODO Auto-generated method stub
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 2000;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
        boolean flag = !this.findSoul(playerIn).isEmpty();

        if (!playerIn.capabilities.isCreativeMode && !flag)
        {
            return flag ? new ActionResult(EnumActionResult.PASS, itemstack) : new ActionResult(EnumActionResult.FAIL, itemstack);
        }
        else
        {
            playerIn.setActiveHand(handIn);
            return new ActionResult(EnumActionResult.SUCCESS, itemstack);
        }
	}
	
	private ItemStack findSoul(EntityPlayer player)
    {
        if (this.isSoul(player.getHeldItem(EnumHand.OFF_HAND)))
        {
            return player.getHeldItem(EnumHand.OFF_HAND);
        }
        else if (this.isSoul(player.getHeldItem(EnumHand.MAIN_HAND)))
        {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        }
        else
        {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
            {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (this.isSoul(itemstack))
                {
                    return itemstack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    protected boolean isSoul(ItemStack stack)
    {
        return stack.getItem() instanceof ItemSoulInABottle;
    }
	
	
}
