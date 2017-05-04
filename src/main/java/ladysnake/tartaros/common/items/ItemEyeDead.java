package ladysnake.tartaros.common.items;

import java.util.List;

import javax.annotation.Nullable;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.entity.EntityMinion;
import ladysnake.tartaros.common.entity.EntityMinionZombie;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.inventory.Helper;
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
import net.minecraft.util.math.AxisAlignedBB;
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
		this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID + ":fueled"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				// System.out.println(entityIn != null &&
				// (!Helper.findItem((EntityPlayer)entityIn,
				// ModItems.SOUL_IN_A_BOTTLE).isEmpty()) ? 1.0F : 0.0F);
				return entityIn != null
						&& (!Helper.findItem((EntityPlayer) entityIn, ModItems.SOUL_IN_A_BOTTLE).isEmpty()) ? 1.0F
								: 0.0F;
			}
		});
		this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID + ":resurrecting"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				// System.out.println(entityIn == null ? 0.0F :
				// (entityIn.getActiveItemStack().getItem() !=
				// ModItems.EYE_OF_THE_UNDEAD ? 0.0F :
				// (float)(stack.getMaxItemUseDuration() -
				// entityIn.getItemInUseCount()) / 20.0F));
				return entityIn == null ? 0.0F
						: (entityIn.getActiveItemStack().getItem() != ModItems.EYE_OF_THE_UNDEAD ? 0.0F
								: (float) (stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F);
			}
		});
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (!(entityLiving instanceof EntityPlayer) || this.getMaxItemUseDuration(stack) - timeLeft < 30)
			return;
		EntityPlayer player = (EntityPlayer) entityLiving;
		System.out.println(player.capabilities.disableDamage);
		if (IncorporealDataHandler.getHandler(player).isIncorporeal())
			return;
		ItemStack ammo = Helper.findItem(player, ModItems.SOUL_IN_A_BOTTLE);
		if (ammo.isEmpty())
			return;
		stack.damageItem(1, player);
		if (!worldIn.isRemote) {
			ammo.shrink(1);
			EntityMinionZombie minion = new EntityMinionZombie(worldIn, false);
			minion.setPosition(player.posX, player.posY, player.posZ);
			minion.setCorpse(true);
			worldIn.spawnEntity(minion);
		}
		List<EntityMinionZombie> minions = worldIn.getEntitiesWithinAABB(EntityMinionZombie.class, new AxisAlignedBB(Math.floor(entityLiving.posX), Math.floor(entityLiving.posY), Math.floor(entityLiving.posZ), Math.floor(entityLiving.posX) + 1, Math.floor(entityLiving.posY) + 1, Math.floor(entityLiving.posZ) + 1).expandXyz(20));
		for (EntityMinionZombie m : minions) {
			System.out.println(m);
			m.setCorpse(!m.isCorpse());
		}
		ammo.shrink(1);
	}

	

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
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

}
