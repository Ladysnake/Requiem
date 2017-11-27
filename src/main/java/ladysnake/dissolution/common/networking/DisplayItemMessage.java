package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;
import java.util.UUID;

public class DisplayItemMessage implements IMessage {

    ItemStack stack;
    UUID playerUuid;

    public DisplayItemMessage() {}

    public DisplayItemMessage(ItemStack stack, UUID uuid) {
        super();
        this.stack = stack;
        this.playerUuid = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buf2 = new PacketBuffer(buf);
        try {
            this.stack = buf2.readItemStack();
            this.playerUuid = buf2.readUniqueId();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buf2 = new PacketBuffer(buf);
        buf2.writeItemStack(stack);
        buf2.writeUniqueId(playerUuid);
    }

}
