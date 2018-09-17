package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RemnantRespawnMessage implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
        // Nothing to read
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Nothing to write
    }
}
