package ladysnake.requiem.mixin.client.possession;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
    @Unique
    private TexturedButtonWidget supercrafterButton;

    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addSupercrafterButton(CallbackInfo ci) {
        MobEntity possessedEntity = PossessionComponent.get(this.playerInventory.player).getPossessedEntity();
        if (possessedEntity != null && RequiemEntityTypeTags.SUPERCRAFTERS.contains(possessedEntity.getType())) {
            this.supercrafterButton = this.addButton(new TexturedButtonWidget(
                this.x + 5,
                this.height / 2 - 30,
                20,
                18,
                0,
                0,
                19,
                RequiemClient.CRAFTING_BUTTON_TEXTURE,
                (buttonWidget) -> {
                    assert this.client != null;
                    this.onClose();
                    this.client.openScreen(new InventoryScreen(this.playerInventory.player));
                })
            );
        }
    }

    @Dynamic("Lambda method, implementation of PressAction for the crafting book button")
    @Inject(method = "method_19890", at = @At("RETURN"), remap = false)
    private void repositionCraftingButton(ButtonWidget button, CallbackInfo ci) {
        if (this.supercrafterButton != null) {
            this.supercrafterButton.setPos(this.x + 5, this.height / 2 - 30);
        }
    }
}
