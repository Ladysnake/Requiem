package ladysnake.dissolution.common.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ConfigMessage implements IMessage {

    Map<String, String> toSync;

    public ConfigMessage() {
        toSync = new HashMap<>();
    }

    public ConfigMessage(Map<String, String> toSync) {
        this.toSync = toSync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String name = buf.readCharSequence(buf.readByte(), Charset.forName("UTF-8")).toString();
            String value = buf.readCharSequence(buf.readByte(), Charset.forName("UTF-8")).toString();
            toSync.put(name, value);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(toSync.size());
        toSync.forEach((key, value) -> {
            buf.writeByte(key.length());
            buf.writeCharSequence(key, Charset.forName("UTF-8"));
            buf.writeByte(value.length());
            buf.writeCharSequence(value, Charset.forName("UTF-8"));
        });
    }
}
