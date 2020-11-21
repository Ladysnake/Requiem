package ladysnake.requiem.mixin.client.possession;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.client.RequiemClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private static final Identifier POSSESSED_INVENTORY = Requiem.id("textures/gui/no_inventory.png");

    @Unique
    private InventoryLimiter limiter;
    @Unique
    private int previousBackgroundHeight;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.limiter = InventoryLimiter.KEY.get(player);
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"))
    private void scissorEntity(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        RequiemClient.setupInventoryCrop(this.x, this.y, this.backgroundWidth, this.backgroundHeight);
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    private void disableScissor(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        RenderSystem.disableScissor();
    }

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"))
    private Identifier swapBackground(Identifier background) {
        if (this.limiter.useAlternativeInventory()) {
            if (this.backgroundHeight < 185) {
                this.previousBackgroundHeight = this.backgroundHeight;
                this.backgroundHeight = 185;
            }
            return POSSESSED_INVENTORY;
        } else if (this.previousBackgroundHeight > 0) {
            this.backgroundHeight = this.previousBackgroundHeight;
            this.previousBackgroundHeight = 0;
        }
        return background;
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 0
    )
    private int shiftPossessedEntityX(int x) {
        if (this.limiter.useAlternativeInventory()) {
            return x + 40;
        }
        return x;
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 1
    )
    private int shiftPossessedEntityY(int y) {
        if (this.limiter.useAlternativeInventory()) {
            return y + 75;
        }
        return y;
    }
}
