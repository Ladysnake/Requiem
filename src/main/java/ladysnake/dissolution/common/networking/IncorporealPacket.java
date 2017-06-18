package ladysnake.dissolution.common.networking;

import java.util.UUID;

import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class IncorporealPacket implements IMessageHandler<IncorporealMessage, IMessage> {

	@Override
	public IMessage onMessage(final IncorporealMessage message, MessageContext ctx) {
		// just to make sure that the side is correct
		if (ctx.side.isClient()) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				try {
					final EntityPlayer player = Minecraft.getMinecraft().player.world
							.getPlayerEntityByUUID(new UUID(message.playerUUIDMost, message.playerUUIDLeast));
					final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(player);
					playerCorp.setIncorporeal(message.simpleBool, player);
				} catch (NullPointerException e){}
			});
		}
		return null;
	}
}