package ladysnake.dissolution.unused.common.networking;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import ladysnake.dissolution.unused.api.Soul;
import ladysnake.dissolution.unused.api.SoulTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class SoulMessage implements IMessage {

    public static final int FULL_UPDATE = 0;
    public static final int UPDATE_ADD = 1;
    public static final int UPDATE_REMOVE = 2;

    List<Soul> soulList;
    int type;

    public SoulMessage() {
    }

    public SoulMessage(int type, Soul... soulList) {
        this(type, Lists.newArrayList(soulList));
    }

    public SoulMessage(int type, List<Soul> soulList) {
        this.soulList = soulList;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        byte meta = buf.readByte();
        int length = meta & 0x3F;
        type = meta >>> 6;
        soulList = new ArrayList<>(length);
        byte[] buffer = new byte[3];
        for (int i = 0; i < length; i++) {
            buf.readBytes(buffer);
            soulList.add(new Soul(SoulTypes.getById(buffer[0]), buffer[1], buffer[2]));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte((soulList.size() & 0x3F) | this.type << 6);
        soulList.forEach(soul -> buf.writeBytes(new byte[]{soul.getType().getId(), (byte) soul.getPurity(), (byte) soul.getWillingness()}));
    }

}
