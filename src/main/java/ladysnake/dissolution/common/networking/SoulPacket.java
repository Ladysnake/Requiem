package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.ISoulHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SoulPacket implements IMessageHandler<SoulMessage, IMessage> {

	@Override
	public IMessage onMessage(SoulMessage message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			  Minecraft.getMinecraft().addScheduledTask(() -> {
                  final ISoulHandler soulInv = CapabilitySoulHandler.getHandler(Minecraft.getMinecraft().player);
                  switch(message.type) {
                  case SoulMessage.FULL_UPDATE:
                      soulInv.removeAll();
                      soulInv.setSize(message.soulList.size());
                      message.soulList.forEach(soulInv::addSoul);
                      break;
                  case SoulMessage.UPDATE_ADD:
                      message.soulList.forEach(soulInv::addSoul);
                      break;
                  case SoulMessage.UPDATE_REMOVE:
                      message.soulList.forEach(soulInv::removeSoul);
                  }
              });
		  }
		  return null;
	}

}
