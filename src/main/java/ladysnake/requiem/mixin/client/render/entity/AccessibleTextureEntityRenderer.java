package ladysnake.requiem.mixin.client.render.entity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(EntityRenderer.class)
public interface AccessibleTextureEntityRenderer {
    // Mixin strips "get" prefixes for @Invoker in the same way they do for @Accessor
    @Invoker("getTexture")
    @Nullable
    Identifier getTexture(Entity var1);
}
