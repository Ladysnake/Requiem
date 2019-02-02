package ladysnake.dissolution.mixin.client.gui.ingame;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerInventoryScreen.class, CreativePlayerInventoryScreen.class})
public abstract class PlayerInventoryScreenMixin extends AbstractPlayerInventoryScreen<PlayerContainer> {
    public PlayerInventoryScreenMixin(PlayerContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }

    @Inject(
            method = "drawBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/ingame/PlayerInventoryScreen;drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V"
            )
    )
    public void drawPossessedEntity(float tickDelta, int mouseX, int mouseY, CallbackInfo info) {
        Entity cameraEntity = this.client.getCameraEntity();
        if (cameraEntity instanceof DissolutionPlayer && ((DissolutionPlayer) cameraEntity).getPossessionComponent().isPossessing()) {
            LivingEntity possessed = (LivingEntity) ((DissolutionPlayer) cameraEntity).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                //noinspection ConstantConditions
                if ((Object) this instanceof PlayerInventoryScreen) {
                    PlayerInventoryScreen.drawEntity(this.left + 51, this.top + 75, 30, (float)(this.left + 51) - mouseX, (float)(this.top + 75 - 50) - mouseY, possessed);
                } else {
                    PlayerInventoryScreen.drawEntity(this.left + 88, this.top + 45, 20, (float)(this.left + 88 - mouseX), (float)(this.top + 45 - 30 - mouseY), possessed);
                }
            }
        }
    }
}
