package ladysnake.requiem.mixin.entity.attribute;

import ladysnake.requiem.common.entity.ai.attribute.DelegatingAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityAttributes.class)
public abstract class EntityAttributesMixin {
    /**
     * Prevents saving the wrong attributes to disk
     */
    @ModifyVariable(
            method = "toTag(Lnet/minecraft/entity/attribute/EntityAttributeInstance;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(value = "INVOKE", ordinal = 0),
            ordinal = 0
    )
    private static EntityAttributeInstance toTag(EntityAttributeInstance attribute) {
        while (attribute instanceof DelegatingAttribute) {
            attribute = ((DelegatingAttribute) attribute).getOriginal();
        }
        return attribute;
    }
}
