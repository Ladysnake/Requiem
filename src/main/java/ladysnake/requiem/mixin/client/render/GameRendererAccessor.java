package ladysnake.requiem.mixin.client.render;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    // synthetic method corresponding to the lambda in updateTargetedEntity
    @SuppressWarnings("InvokerTarget")
    @Invoker("method_18144")
    static boolean isEligibleForTargeting(Entity tested) {
        return false;
    }
}
