package ladysnake.dissolution.mixin.item;

import ladysnake.dissolution.api.v1.event.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getTooltipText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasTag()Z", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void fireTooltipEvent(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<TextComponent>> cir, List<TextComponent> lines) {
        ItemTooltipCallback.EVENT.invoker().onTooltipBuilt((ItemStack)(Object)this, player, context, lines);
    }
}
