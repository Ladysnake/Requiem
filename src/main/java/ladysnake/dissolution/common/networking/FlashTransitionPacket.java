package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.client.gui.FlashTransitionEffect;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FlashTransitionPacket implements IMessageHandler<FlashTransitionMessage, IMessage> {
    @Override
    public IMessage onMessage(FlashTransitionMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            FlashTransitionEffect.INSTANCE.fade(80);
        }
        return null;
    }
}
