package ladysnake.tartaros.common.capabilities;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IncorporealMessage implements IMessage {

	public IncorporealMessage() {}
	
	private boolean toSend;
	private long playerUUIDMost;
	private long playerUUIDLeast;
	private boolean messageIsValid;
	
	/**
	 * Sends a packet to synchronize incorporeal state between clients
	 * @param isIncorporeal
	 * @param playerUUIDMost
	 * @param playerUUIDLeast
	 */
	public IncorporealMessage(boolean isIncorporeal, long playerUUIDMost, long playerUUIDLeast) {
	    this.toSend = isIncorporeal;
	    this.playerUUIDMost = playerUUIDMost;
	    this.playerUUIDLeast = playerUUIDLeast;
	    messageIsValid = true;
	    System.out.println("message created");
	  }
	
	@Override
	public void fromBytes(ByteBuf buf) {
		System.out.println("fromBytes");
		this.toSend = buf.readBoolean();
		this.playerUUIDMost = buf.readLong();
		this.playerUUIDLeast = buf.readLong();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		System.out.println("toBytes");
		 buf.writeBoolean(this.toSend);
		 buf.writeLong(this.playerUUIDMost);
		 buf.writeLong(playerUUIDLeast);
	}
	
	public boolean isMessageValid() {
		return this.messageIsValid;
	}
	
	@SideOnly(Side.CLIENT)
	public static class IncorporealMessageHandler implements IMessageHandler<IncorporealMessage, IMessage> {

		@Override
		public IMessage onMessage(final IncorporealMessage message, MessageContext ctx) {

			System.out.println("a packet has been received");
			if (ctx.side != Side.CLIENT) {
			      System.err.println("TargetEffectMessageToClient received on wrong side:" + ctx.side);
			      return null;
			    }
			    if (!message.isMessageValid()) {
			      System.err.println("TargetEffectMessageToClient was invalid" + message.toString());
			      return null;
			}
			
			Minecraft.getMinecraft().addScheduledTask(new Runnable()
			{
			  public void run() {
				  System.out.println("a packet has been processed");
				  final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player.world.getPlayerEntityByUUID(new UUID(message.playerUUIDMost, message.playerUUIDLeast)));
				  playerCorp.setIncorporeal(message.toSend);
				  System.out.println(playerCorp);
			  }
			});
			return null;
		}
		
	}

}
