package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
                    CapabilityIncorporealHandler.getHandler(player).setPossessed((EntityLivingBase & IPossessable) possessed, true);
                }
            });
        }
        return null;
    }
}
