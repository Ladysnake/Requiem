package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.Ref;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper NET = NetworkRegistry.INSTANCE.newSimpleChannel(Ref.MOD_ID.toUpperCase());

    private static int nextPacketId = 0;

    public static void initPackets() {
        NET.registerMessage(IncorporealPacket.class, IncorporealMessage.class, nextPacketId++, Side.CLIENT);
        NET.registerMessage(PingPacket.class, PingMessage.class, nextPacketId++, Side.SERVER);
        NET.registerMessage(DisplayItemPacket.class, DisplayItemMessage.class, nextPacketId++, Side.CLIENT);
        NET.registerMessage(PossessionPacket.class, PossessionMessage.class, nextPacketId++, Side.CLIENT);
        NET.registerMessage(ConfigPacket.class, ConfigMessage.class, nextPacketId++, Side.CLIENT);
        NET.registerMessage(FlashTransitionPacket.class, FlashTransitionMessage.class, nextPacketId++, Side.CLIENT);
    }
}