package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PossessionPacket implements IMessageHandler<PossessionMessage, IMessage> {

    @Override
    public IMessage onMessage(PossessionMessage message, MessageContext ctx) {
        if (ctx.side.isClient())
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity possessed = Minecraft.getMinecraft().world.getEntityByID(message.possessedUuid);
                EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.playerUuid);
                if (player != null)
                    CapabilityIncorporealHandler.getHandler(player).setPossessed((IPossessable) possessed);
            });
        else {
            final World world = ctx.getServerHandler().player.world;
            assert world.getMinecraftServer() != null;
            world.getMinecraftServer().addScheduledTask(() -> {
                Entity possessed = world.getEntityByID(message.possessedUuid);
                EntityPlayer player = world.getPlayerEntityByUUID(message.playerUuid);
                if (possessed instanceof IPossessable) {
                    if (player == null || possessed.getDistanceSq(player) < 1024)
                        ((IPossessable) possessed).onEntityPossessed(player != null && CapabilityIncorporealHandler
                                .getHandler(player).getCorporealityStatus().isIncorporeal()
                                ? player
                                : null);
                }
            });
        }
        return null;
    }
}
