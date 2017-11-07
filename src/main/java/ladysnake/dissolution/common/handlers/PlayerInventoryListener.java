package ladysnake.dissolution.common.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ladysnake.dissolution.common.items.ItemScythe;
import ladysnake.dissolution.common.networking.DisplayItemMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class PlayerInventoryListener implements IContainerListener {
	
	private static Map<UUID, ItemStack> itemToDisplay = new HashMap<>();
	private static Map<UUID, Integer> itemSlotForDisplay = new HashMap<>();
	
	private EntityPlayerMP player;
	
	public static ItemStack getItemToDisplay(UUID playerUUID) {
		return itemToDisplay.getOrDefault(playerUUID, ItemStack.EMPTY);
	}
	
	public static void setItemToDisplay(UUID playerUUID, ItemStack stack) {
		itemToDisplay.put(playerUUID, stack);
	}
	
	public PlayerInventoryListener(EntityPlayerMP player) {
		super();
		this.player = player;
		PacketHandler.NET.sendToAllAround(new DisplayItemMessage(findItemToDisplay(),
				player.getUniqueID()), new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		PacketHandler.NET.sendToAllAround(new DisplayItemMessage(findItemToDisplay(),
				player.getUniqueID()), new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));
	}
	
	private ItemStack findItemToDisplay() {
		for(ItemStack stack : player.inventory.mainInventory) {
			if(stack.getItem() instanceof ItemScythe) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}

	@Override
	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}

}
