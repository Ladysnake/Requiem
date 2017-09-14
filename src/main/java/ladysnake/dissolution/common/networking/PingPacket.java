package ladysnake.dissolution.common.networking;

import java.util.UUID;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PingPacket implements IMessageHandler<PingMessage, UpdateMessage>
{
  
	@Override
	public UpdateMessage onMessage(final PingMessage message, final MessageContext ctx) 
	{
	  // just to make sure that the side is correct
	  if (ctx.side.isServer())
	  {
		  FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				  EntityPlayerMP thePlayer = ctx.getServerHandler().player;
				  final IIncorporealHandler clone = CapabilityIncorporealHandler.getHandler(thePlayer);
				  PacketHandler.net.sendToAll(new IncorporealMessage(message.uuidMost, message.uuidLeast, clone.isIncorporeal() || clone.isIncorporeal()));
				  PacketHandler.net.sendTo(new SoulMessage(SoulMessage.FULL_UPDATE, CapabilitySoulHandler.getHandler(thePlayer).getSoulList()), thePlayer);
		  });
	  }
	  return null;
	}
}