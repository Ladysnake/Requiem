package ladysnake.dissolution.common.items;

import java.util.List;

import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.capabilities.Soul;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.handlers.CustomDissolutionTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDebug extends Item implements ISoulInteractable {
	
	protected int debugWanted = 0;

	public ItemDebug() {
		super();
		this.setUnlocalizedName(Reference.Items.DEBUG.getUnlocalizedName());
        this.setRegistryName(Reference.Items.DEBUG.getRegistryName());
        this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(playerIn.isSneaking()) {
			if(!worldIn.isRemote) {
				debugWanted = (debugWanted + 1) % 8;
				playerIn.sendStatusMessage(new TextComponentString("debug: " + debugWanted), true);
			}
			return super.onItemRightClick(worldIn, playerIn, handIn);
		}
		switch(debugWanted) {
		case 0 : 
			if(worldIn.isRemote) {
				ResourceLocation shader = new ResourceLocation("minecraft:shaders/post/intangible.json");
				if(Minecraft.getMinecraft().entityRenderer.isShaderActive())
					Minecraft.getMinecraft().entityRenderer.stopUseShader();
				else
					Minecraft.getMinecraft().entityRenderer.loadShader(shader);
			}
			break;
		case 1 :	
			CapabilityIncorporealHandler.getHandler(playerIn).setIncorporeal(!CapabilityIncorporealHandler.getHandler(playerIn).isIncorporeal());
			break;
		case 2 :
			if(!playerIn.world.isRemote)
				CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) playerIn, playerIn.dimension == -1 ? 0 : -1);
			break;
		case 3 :
			if(!playerIn.world.isRemote) {
				DissolutionConfig.ghost.flightMode = DissolutionConfig.ghost.flightMode + 1;
				if(DissolutionConfig.ghost.flightMode > 2) DissolutionConfig.ghost.flightMode = -1;
				playerIn.sendStatusMessage(new TextComponentString("flight mode : " + DissolutionConfig.ghost.flightMode), true);
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
			List<EntityPlayerCorpse> playerCorpses = playerIn.world.getEntities(EntityPlayerCorpse.class, e -> e.getDistanceToEntity(playerIn) < 20);
			playerCorpses.forEach(corpse -> {
				playerIn.sendStatusMessage(new TextComponentString(playerIn.world.isRemote ? "clientSide" : "serverSide"), false);
				corpse.getSoulHandlerCapability().forEach(soul -> playerIn.sendStatusMessage(new TextComponentString(soul.toString()), false));
			});
			playerIn.sendStatusMessage(new TextComponentString("player - " + (playerIn.world.isRemote ? "clientSide" : "serverSide")), false);
			CapabilitySoulHandler.getHandler(playerIn).forEach(soul -> playerIn.sendStatusMessage(new TextComponentString(soul.toString()), false));
		case 7 :
			List<EntityPlayerCorpse> playerCorpses2 = playerIn.world.getEntities(EntityPlayerCorpse.class, e -> e.getDistanceToEntity(playerIn) < 20);
			playerCorpses2.forEach(corpse -> corpse.getSoulHandlerCapability().addSoul(Soul.UNDEFINED));
		default : break;
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
