package ladysnake.dissolution.common.items;

import java.util.List;
import java.util.UUID;

import ladysnake.dissolution.api.IEssentiaHandler;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.capabilities.CapabilityEssentiaHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.handlers.CustomDissolutionTeleporter;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemDebug extends Item implements ISoulInteractable {
	
	protected int debugWanted = 0;

	public ItemDebug() {
		super();
        this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(playerIn.isSneaking()) {
			if(!worldIn.isRemote) {
				debugWanted = (debugWanted + 1) % 8;
				playerIn.sendStatusMessage(new TextComponentString("debug: " + debugWanted), true);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
		switch(debugWanted) {
		case 0 : 
			CapabilityIncorporealHandler.getHandler(playerIn).setIncorporeal(!CapabilityIncorporealHandler.getHandler(playerIn).isIncorporeal());
			break;
		case 1 :	
			if(worldIn.isRemote) {
				ResourceLocation shader = new ResourceLocation("minecraft:shaders/post/intangible.json");
				if(Minecraft.getMinecraft().entityRenderer.isShaderActive())
					Minecraft.getMinecraft().entityRenderer.stopUseShader();
				else
					Minecraft.getMinecraft().entityRenderer.loadShader(shader);
			}
			break;
		case 2 :
			if(!playerIn.world.isRemote)
				CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) playerIn, playerIn.dimension == -1 ? 0 : -1);
			break;
		case 3 :
			if(!playerIn.world.isRemote) {
				worldIn.loadedEntityList.stream().filter(e -> e instanceof EntityFleetingSoul).forEach(e -> e.onKillCommand());
				EntityFleetingSoul cam = new EntityFleetingSoul(playerIn.world, playerIn.posX + 2, playerIn.posY, playerIn.posZ);
				worldIn.spawnEntity(cam);
			} 
			break;
		case 4 :
			if(!playerIn.world.isRemote) {
				playerIn.sendStatusMessage(new TextComponentString("Printing fire information"), true);
				List<Entity> fires = playerIn.world.getEntities(Entity.class, e -> e.getDistanceToEntity(playerIn) < 20);
				fires.forEach(System.out::println);
			}
			break;
		case 5 :
			if(playerIn.world.isRemote) {
				ShaderHelper.initShaders();
				playerIn.sendStatusMessage(new TextComponentString("Reloaded shaders"), false);
			}
			break;
		case 6 :
			if(!worldIn.isRemote) {
				RayTraceResult result = playerIn.rayTrace(6, 0);
				TileEntity te = worldIn.getTileEntity(result.getBlockPos());
				if(te != null && te.hasCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, result.sideHit)) {
					IEssentiaHandler essentiaInv = te.getCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, result.sideHit);
					playerIn.sendStatusMessage(new TextComponentTranslation("suction: %s, type: %s", essentiaInv.getSuction(), essentiaInv.getSuctionType()), false);
				}
			}
			break;
		case 7 :
			ItemStack eye = new ItemStack(ModItems.EYE_OF_THE_UNDEAD);
			ModItems.EYE_OF_THE_UNDEAD.setShell(eye, new ItemStack(ModItems.GOLD_SHELL));
			playerIn.addItemStackToInventory(eye);
			break;
		default : break;
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
}
