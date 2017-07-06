package ladysnake.dissolution.common.networking;

import ladysnake.dissolution.common.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
  public static SimpleNetworkWrapper net;
  
  public static void initPackets()
  {
    net = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID.toUpperCase());
    registerMessage(IncorporealPacket.class, IncorporealMessage.class);
    registerMessage(PingPacket.class, PingMessage.class);
    registerMessage(SoulPacket.class, SoulMessage.class);
  }
  
  private static int nextPacketId = 0;
  
  private static void registerMessage(Class packet, Class message)
  {
    net.registerMessage(packet, message, nextPacketId, Side.CLIENT);
    net.registerMessage(packet, message, nextPacketId, Side.SERVER);
    nextPacketId++;
  }
}