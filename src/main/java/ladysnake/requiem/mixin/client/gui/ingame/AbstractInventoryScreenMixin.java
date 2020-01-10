package ladysnake.requiem.mixin.client.gui.ingame;

import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.mixin.client.texture.SpriteAtlasHolderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.container.Container;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends Container> extends AbstractContainerScreen<T> {
    @Unique
    private StatusEffectInstance renderedEffect;
    @Unique
    private boolean boundSpecialBackground;

    public AbstractInventoryScreenMixin(T container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    // ModifyVariable is only used to capture the local variable more easily
    // we cannot use INVOKE_ASSIGN and Iterator#next, because there is a hidden cast instruction
    @ModifyVariable(method = "drawStatusEffectBackgrounds", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V"))
    private StatusEffectInstance customizeDrawnBackground(StatusEffectInstance effect) {
        if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
            assert minecraft != null;
            minecraft.getTextureManager().bindTexture(AttritionStatusEffect.ATTRITION_BACKGROUND);
            boundSpecialBackground = true;
        }
        return effect;
    }

    @Inject(method = "drawStatusEffectBackgrounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;blit(IIIIII)V", shift = At.Shift.AFTER))
    private void restoreDrawnBackground(int x, int yIncrement, Iterable<StatusEffectInstance> effects, CallbackInfo ci) {
        if (boundSpecialBackground) {
            assert minecraft != null;
            minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
            boundSpecialBackground = false;
        }
    }

    @ModifyVariable(method = "drawStatusEffectSprites", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getEffectType()Lnet/minecraft/entity/effect/StatusEffect;"))
    private StatusEffectInstance captureCurrentEffect(StatusEffectInstance effect) {
        this.renderedEffect = effect;
        return effect;
    }

    @ModifyVariable(method = "drawStatusEffectSprites", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/texture/StatusEffectSpriteManager;getSprite(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite customizeDrawnSprite(Sprite baseSprite) {
        int amplifier = renderedEffect.getAmplifier();
        if (this.renderedEffect.getEffectType() == RequiemStatusEffects.ATTRITION && amplifier < 4) {
            Identifier baseId = baseSprite.getId();
            return ((SpriteAtlasHolderAccessor) MinecraftClient.getInstance().getStatusEffectSpriteManager())
                .getAtlas().getSprite(new Identifier(baseId.getNamespace(), baseId.getPath() + '_' + (amplifier + 1)));
        }
        return baseSprite;
    }
}
