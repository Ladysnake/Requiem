package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.capabilities.ISoulInventoryHandler;
import ladysnake.dissolution.common.capabilities.Soul;
import ladysnake.dissolution.common.capabilities.SoulInventoryDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SoulPacket implements IMessageHandler<SoulMessage, IMessage> {

	@Override
	public IMessage onMessage(SoulMessage message, MessageContext ctx) {
		if (ctx.side.isClient())
		  {
			  Minecraft.getMinecraft().addScheduledTask(new Runnable()
				{
				  public void run() {
					  final ISoulInventoryHandler soulInv = SoulInventoryDataHandler.getHandler(Minecraft.getMinecraft().player);
					  System.out.println("packet type: " + message.type);
					  switch(message.type) {
					  case SoulMessage.FULL_UPDATE:
						  soulInv.getSoulList().clear();
						  soulInv.getSoulList().addAll(message.soulList);
						  break;
					  case SoulMessage.UPDATE_ADD:
						  soulInv.getSoulList().addAll(message.soulList);
						  break;
					  case SoulMessage.UPDATE_REMOVE:
						  soulInv.getSoulList().removeAll(message.soulList);
					  }
				  }
				});
		  }
		  return null;
	}

}
