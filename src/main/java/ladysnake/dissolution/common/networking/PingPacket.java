package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PingPacket implements IMessageHandler<PingMessage, IMessage> {

    @Override
    public IMessage onMessage(final PingMessage message, final MessageContext ctx) {
        // just to make sure that the side is correct
        if (ctx.side.isServer()) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayerMP thePlayer = ctx.getServerHandler().player;
                final IIncorporealHandler clone = CapabilityIncorporealHandler.getHandler(thePlayer);
                PacketHandler.NET.sendToAll(new IncorporealMessage(message.uuidMost, message.uuidLeast, clone.isStrongSoul(), clone.getCorporealityStatus()));
//			  PacketHandler.NET.sendTo(new SoulMessage(SoulMessage.FULL_UPDATE, CapabilitySoulHandler.getHandler(thePlayer).getSoulList()), thePlayer);
                IPossessable possessed = clone.getPossessed();
                if (possessed instanceof Entity) {
                    PacketHandler.NET.sendTo(new PossessionMessage(thePlayer.getUniqueID(), ((Entity) possessed).getEntityId()), thePlayer);
                    thePlayer.connection.sendPacket(new SPacketCamera((Entity) possessed));
                }
                clone.getDialogueStats().checkFirstConnection();
            });
        }
        return null;
    }
}