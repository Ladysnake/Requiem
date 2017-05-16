package ladysnake.dissolution.common.networking;

import java.util.UUID;

import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
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
		//System.out.println("message get !");
	  // just to make sure that the side is correct
	  if (ctx.side.isServer())
	  {
		  FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable()
			{
			  public void run() {
				  //System.out.println("a ping packet has been processed");
				  EntityPlayerMP thePlayer = ctx.getServerHandler().playerEntity;
				  final IIncorporealHandler clone = IncorporealDataHandler.getHandler((EntityPlayer)thePlayer);
				  IMessage msg = new IncorporealMessage(message.uuidMost, message.uuidLeast, clone.isIncorporeal() || clone.isIncorporeal());
				  PacketHandler.net.sendToAll(msg);
			  }
			});
	  }
	  return null;
	}
}