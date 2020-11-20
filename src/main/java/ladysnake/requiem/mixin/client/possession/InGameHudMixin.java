package ladysnake.requiem.mixin.client.possession;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.common.impl.inventory.PlayerInventoryLimiter;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final int X_CENTER_SHIFT = 77;

    @Unique
    private boolean cancelNextItem;
    @Unique
    private boolean renderMainHandOnly;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;WIDGETS_TEXTURE:Lnet/minecraft/util/Identifier;"))
    private void checkInventoryLimit(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        this.renderMainHandOnly = InventoryLimiter.KEY.get(this.getCameraPlayer()).isMainInventoryLocked();
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 1
    )
    private int centerCroppedHotbar(int x) {
        if (this.renderMainHandOnly) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 2
    )
    private int cropHotbar_y(int y) {
        if (this.renderMainHandOnly) {
            return this.scaledHeight - 23;
        }
        return y;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 3
    )
    private int cropHotbar_u(int u) {
        if (this.renderMainHandOnly) {
            return 24;
        }
        return u;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 4
    )
    private int cropHotbar_v(int v) {
        if (this.renderMainHandOnly) {
            return 22;
        }
        return v;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 5
    )
    private int cropHotbar_width(int width) {
        if (this.renderMainHandOnly) {
            return 29;
        }
        return width;
    }

    @ModifyArg(
        method = "renderHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0),
        index = 6
    )
    private int cropHotbar_height(int height) {
        if (this.renderMainHandOnly) {
            return 24;
        }
        return height;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;")
    )
    private int cancelLockedItemRender(int index) {
        this.cancelNextItem = renderMainHandOnly && index != PlayerInventoryLimiter.MAINHAND_SLOT;
        return index;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
    )
    private ItemStack cancelLockedItemRender(ItemStack stack) {
        if (this.cancelNextItem) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "renderHotbar",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1)
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V"),
        index = 0
    )
    private int shiftMainHandItem(int x) {
        if (this.renderMainHandOnly && !this.cancelNextItem) {
            return x + X_CENTER_SHIFT;
        }
        return x;
    }
}
