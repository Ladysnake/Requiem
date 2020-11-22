package ladysnake.requiem.mixin.client.possession;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.InventoryScreenAccessor;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements InventoryScreenAccessor {
    @Unique
    private static final Identifier INVENTORY_SLOTS = Requiem.id("textures/gui/inventory_slots.png");

    @Unique
    private InventoryLimiter limiter;
    @Unique
    private PossessionComponent possessionComponent;
    @Unique
    private AbstractButtonWidget craftingBookButton;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Override
    public @NotNull AbstractButtonWidget requiem_getRecipeBookButton() {
        return this.craftingBookButton;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addButton(Lnet/minecraft/client/gui/widget/AbstractButtonWidget;)Lnet/minecraft/client/gui/widget/AbstractButtonWidget;"), allow = 1)
    private AbstractButtonWidget captureCraftingBookButton(AbstractButtonWidget button) {
        this.craftingBookButton = button;

        if (this.limiter.isLocked(InventoryPart.CRAFTING)) {
            button.visible = false;
        }

        return button;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.limiter = InventoryLimiter.KEY.get(player);
        this.possessionComponent = PossessionComponent.get(player);
    }

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    private Text swapScreenName(Text name) {
        MobEntity possessedEntity = this.possessionComponent.getPossessedEntity();
        if (possessedEntity != null) {
            return possessedEntity.getName();
        }
        return name;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"))
    private void scissorEntity(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        this.limiter.getInventoryShape().setupEntityCrop(this.x, this.y);
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    private void disableScissor(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        this.limiter.getInventoryShape().tearDownEntityCrop();
    }

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"))
    private Identifier swapBackground(Identifier background) {
        return this.limiter.getInventoryShape().swapBackground(background);
    }

    @Inject(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
            shift = At.Shift.AFTER
        )
    )
    private void drawSlots(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        assert this.client != null;

        InventoryShape inventoryShape = this.limiter.getInventoryShape();
        if (inventoryShape.isAltShape()) {
            this.client.getTextureManager().bindTexture(INVENTORY_SLOTS);
            int x = this.x;
            int y = this.y;
            if (!this.limiter.isLocked(InventoryPart.ARMOR)) {
                this.drawTexture(matrices, x + 7, y + 7, 7, 7, 18, 72);
            }
            if (!this.limiter.isLocked(InventoryPart.HANDS)) {
                if (inventoryShape == InventoryShape.ALT_SMALL) {
                    this.drawTexture(matrices, x + 76, y + 61, 76, 61, 18, 18);
                } else {
                    this.drawTexture(matrices, x + 46, y + 61, 46, 61, 48, 18);
                }
            }
            if (!this.limiter.isLocked(InventoryPart.CRAFTING)) {
                this.drawTexture(matrices, x + 97, y + 17, 97, 17, 75, 36);
            }
            if (!this.limiter.isLocked(InventoryPart.MAIN)) {
                this.drawTexture(matrices, x + 7, y + 83, 7, 83, 162, 76);
            }
        }
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 0
    )
    private int shiftPossessedEntityX(int x) {
        return this.limiter.getInventoryShape().shiftEntityX(x);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 1
    )
    private int shiftPossessedEntityY(int y) {
        return this.limiter.getInventoryShape().shiftEntityY(y);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 3
    )
    private int shiftPossessedEntityLookX(int x) {
        return this.limiter.getInventoryShape().shiftEntityX(x);
    }

    @ModifyArg(
        method = "drawBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"),
        index = 4
    )
    private int shiftPossessedEntityLookY(int y) {
        return this.limiter.getInventoryShape().shiftEntityY(y);
    }
}
