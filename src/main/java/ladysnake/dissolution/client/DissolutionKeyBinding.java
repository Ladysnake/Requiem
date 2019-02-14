package ladysnake.dissolution.client;

import ladysnake.dissolution.Dissolution;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createEtherealFractureMessage;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToServer;

public class DissolutionKeyBinding {

    public static final Identifier ETHEREAL_FRACTURE = Dissolution.id("ethereal_fracture");

    public static final FabricKeyBinding etherealFractureKey = FabricKeyBinding.Builder.create(
            ETHEREAL_FRACTURE,
            InputUtil.Type.KEY_KEYBOARD,
            GLFW.GLFW_KEY_WORLD_2,
            "key.categories.gameplay"
    ).build();

    public static void init() {
        KeyBindingRegistry.INSTANCE.register(etherealFractureKey);
    }

    public static void update(MinecraftClient client) {
        if (client.player != null && etherealFractureKey.wasPressed()) {
            sendToServer(createEtherealFractureMessage());
        }
    }
}
