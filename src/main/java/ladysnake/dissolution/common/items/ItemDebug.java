package ladysnake.dissolution.common.items;

import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.handlers.CustomTartarosTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemDebug extends Item {

	public ItemDebug() {
		super();
		this.setUnlocalizedName(Reference.Items.DEBUG.getUnlocalizedName());
        this.setRegistryName(Reference.Items.DEBUG.getRegistryName());
        this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		List<EntityMinionZombie> minions = worldIn.getEntitiesWithinAABB(EntityMinionZombie.class, new AxisAlignedBB(Math.floor(playerIn.posX), Math.floor(playerIn.posY), Math.floor(playerIn.posZ), Math.floor(playerIn.posX) + 1, Math.floor(playerIn.posY) + 1, Math.floor(playerIn.posZ) + 1).expandXyz(20));
		for (EntityMinionZombie m : minions) {
			System.out.println(m);
			m.setCorpse(!m.isCorpse());
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
