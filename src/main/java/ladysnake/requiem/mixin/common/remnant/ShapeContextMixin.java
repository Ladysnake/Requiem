package ladysnake.requiem.mixin.common.remnant;

import ladysnake.requiem.common.util.ExtendedShapeContext;
import net.minecraft.block.ShapeContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShapeContext.class)
public interface ShapeContextMixin extends ExtendedShapeContext {
    @Override
    default boolean requiem_isNoClipping() {
        return false;
    }
}
