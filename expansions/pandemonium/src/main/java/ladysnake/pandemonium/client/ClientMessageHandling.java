package ladysnake.pandemonium.client;

import ladysnake.requiem.client.RequiemClient;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.ANCHOR_DAMAGE;
import static ladysnake.pandemonium.common.network.PandemoniumNetworking.ETHEREAL_ANIMATION;

public class ClientMessageHandling {
    private static final float[] ETHEREAL_DAMAGE_COLOR = {0.5f, 0.0f, 0.0f};

    public static void init() {
        ClientSidePacketRegistry.INSTANCE.register(ANCHOR_DAMAGE, ((context, buf) -> {
            boolean dead = buf.readBoolean();
            context.getTaskQueue().execute(() -> RequiemClient.INSTANCE.getRequiemFxRenderer().playEtherealPulseAnimation(
                dead ? 4 : 1, ETHEREAL_DAMAGE_COLOR[0], ETHEREAL_DAMAGE_COLOR[1], ETHEREAL_DAMAGE_COLOR[2]
            ));
        }));
        ClientSidePacketRegistry.INSTANCE.register(ETHEREAL_ANIMATION, ((context, buf) -> context.getTaskQueue().execute(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            assert mc != null;
            assert mc.player != null;
            mc.player.world.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2, 0.6f);
            RequiemClient.INSTANCE.getRequiemFxRenderer().beginEtherealAnimation();
        })));
    }

}
