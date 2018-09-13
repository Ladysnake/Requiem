package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IncorporealPacket implements IMessageHandler<IncorporealMessage, IMessage> {

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(final IncorporealMessage message, MessageContext ctx) {
        // just to make sure that the side is correct
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final Entity player = Minecraft.getMinecraft().player.world.getEntityByID(message.playerId);
                CapabilityIncorporealHandler.getHandler(player).ifPresent(playerCorp -> {
                    playerCorp.setStrongSoul(message.strongSoul);
                    playerCorp.setCorporealityStatus(message.corporealityStatus);
                });
            });
        }
        return null;
    }
}