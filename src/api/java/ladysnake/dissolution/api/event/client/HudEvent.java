package ladysnake.dissolution.api.event.client;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.util.ActionResult;

public final class HudEvent {
    private HudEvent() { throw new AssertionError(); }

    public static final HandlerRegistry<Float2ObjectFunction<ActionResult>> RENDER_HOTBAR = new HandlerArray<>(Float2ObjectFunction.class);

}
