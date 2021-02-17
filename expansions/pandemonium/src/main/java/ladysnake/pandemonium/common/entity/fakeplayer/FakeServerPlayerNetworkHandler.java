package ladysnake.pandemonium.common.entity.fakeplayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class FakeServerPlayerNetworkHandler extends ServerPlayNetworkHandler {
    public FakeServerPlayerNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player) {
        super(server, connection, player);
    }
}
