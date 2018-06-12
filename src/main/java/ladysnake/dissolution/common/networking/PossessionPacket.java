package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PossessionPacket implements IMessageHandler<PossessionMessage, IMessage> {

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PossessionMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                Entity possessed = mc.world.getEntityByID(message.possessedUuid);
                EntityPlayer player = mc.world.getPlayerEntityByUUID(message.playerUuid);
                if (player != null) {
                    CapabilityIncorporealHandler.getHandler(player).setPossessed((EntityMob & IPossessable) possessed);
                }
            });
        } else {
            final World world = ctx.getServerHandler().player.world;
            assert world.getMinecraftServer() != null;
            world.getMinecraftServer().addScheduledTask(() -> {
                Entity possessed = world.getEntityByID(message.possessedUuid);
                EntityPlayer player = world.getPlayerEntityByUUID(message.playerUuid);
                if (possessed instanceof IPossessable) {
                    if (player == null || possessed.getDistanceSq(player) < 1024) {
                        ((IPossessable) possessed).onEntityPossessed(player != null && CapabilityIncorporealHandler
                                .getHandler(player).getCorporealityStatus().isIncorporeal()
                                ? player
                                : null);
                    }
                }
            });
        }
        return null;
    }
}
