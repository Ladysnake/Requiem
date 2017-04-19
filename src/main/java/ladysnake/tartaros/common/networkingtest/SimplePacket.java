package ladysnake.tartaros.common.networkingtest;

import java.util.UUID;

import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SimplePacket implements IMessageHandler<SimpleMessage, IMessage>
{
  
	@Override
	public IMessage onMessage(final SimpleMessage message, MessageContext ctx) 
	{
		System.out.println("message get !");
	  // just to make sure that the side is correct
	  if (ctx.side.isClient())
	  {
		  Minecraft.getMinecraft().addScheduledTask(new Runnable()
			{
			  public void run() {
				  System.out.println("a packet has been processed");
				  final EntityPlayer player = Minecraft.getMinecraft().player.world.getPlayerEntityByUUID(new UUID(message.playerUUIDMost, message.playerUUIDLeast));
				  final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(player);
				  playerCorp.setIncorporeal(message.simpleBool, player);
				  System.out.println("client" + playerCorp);				  
			  }
			});
	  }
	  return null;
	}
}