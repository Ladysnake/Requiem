package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class IncorporealPacket implements IMessageHandler<IncorporealMessage, IMessage> {

	@Override
	public IMessage onMessage(final IncorporealMessage message, MessageContext ctx) {
		// just to make sure that the side is correct
		if (ctx.side.isClient()) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				try {
					final EntityPlayer player = Minecraft.getMinecraft().player.world
							.getPlayerEntityByUUID(new UUID(message.playerUUIDMost, message.playerUUIDLeast));
					final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
					playerCorp.setStrongSoul(message.strongSoul);
					playerCorp.setCorporealityStatus(message.corporealityStatus);
				} catch (NullPointerException ignored){}
			});
		}
		return null;
	}
}