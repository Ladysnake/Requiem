package ladysnake.dissolution.common.items;

import java.util.List;

import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.client.renders.entities.RenderPlayerCorpse;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.capabilities.SoulInventoryDataHandler;
import ladysnake.dissolution.common.entity.EntityBrimstoneFire;
import ladysnake.dissolution.common.handlers.CustomTartarosTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
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
				debugWanted = (debugWanted + 1) % 7;
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
			IncorporealDataHandler.getHandler(playerIn).setIncorporeal(!IncorporealDataHandler.getHandler(playerIn).isIncorporeal(), playerIn);
			break;
		case 2 :
			if(!playerIn.world.isRemote)
				CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) playerIn, playerIn.dimension == -1 ? 0 : -1);
			break;
		case 3 :
			if(!playerIn.world.isRemote) {
				DissolutionConfig.flightMode = DissolutionConfig.flightMode + 1;
				if(DissolutionConfig.flightMode > 2) DissolutionConfig.flightMode = -1;
				playerIn.sendStatusMessage(new TextComponentString("flight mode : " + DissolutionConfig.flightMode), true);
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
			playerIn.sendStatusMessage(new TextComponentString(playerIn.world.isRemote ? "clientSide" : "serverSide"), false);
			SoulInventoryDataHandler.getHandler(playerIn).forEach(soul -> playerIn.sendStatusMessage(new TextComponentString(soul.toString()), false));
		default : break;
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
