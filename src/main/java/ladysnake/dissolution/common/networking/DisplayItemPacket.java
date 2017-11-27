package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.handlers.PlayerInventoryListener;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DisplayItemPacket implements IMessageHandler<DisplayItemMessage, IMessage> {

    @Override
    public IMessage onMessage(DisplayItemMessage message, MessageContext ctx) {
        if (ctx.side.isClient())
            Minecraft.getMinecraft().addScheduledTask(() -> PlayerInventoryListener.setItemToDisplay(message.playerUuid, message.stack));
        return null;
    }

}
