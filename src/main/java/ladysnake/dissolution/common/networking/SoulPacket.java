package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.capabilities.ISoulHandler;
import ladysnake.dissolution.common.capabilities.Soul;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
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
					  final ISoulHandler soulInv = CapabilitySoulHandler.getHandler(Minecraft.getMinecraft().player);
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
