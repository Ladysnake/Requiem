package ladysnake.requiem.client;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToServer;

public class RequiemKeyBinding {

    public static final Identifier ETHEREAL_FRACTURE = Requiem.id("ethereal_fracture");

    public static final FabricKeyBinding etherealFractureKey = FabricKeyBinding.Builder.create(
            ETHEREAL_FRACTURE,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_WORLD_2,
            "key.categories.gameplay"
    ).build();

    public static void init() {
        KeyBindingRegistry.INSTANCE.register(etherealFractureKey);
    }

    public static void update(MinecraftClient client) {
        if (client.player != null && etherealFractureKey.wasPressed()) {
            sendToServer(ETHEREAL_FRACTURE, createEmptyBuffer());
        }
    }
}
