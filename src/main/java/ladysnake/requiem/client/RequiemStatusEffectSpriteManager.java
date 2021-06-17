package ladysnake.requiem.client;


import ladysnake.requiem.mixin.client.attrition.SpriteAtlasHolderAccessor;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class RequiemStatusEffectSpriteManager implements ClientSpriteRegistryCallback {
    private final Map<Identifier, Identifier[]> spriteMappings = new HashMap<>();

    public void registerAltSprites(StatusEffect effect, int altSpriteCount) {
        Identifier baseId = net.minecraft.util.registry.Registry.STATUS_EFFECT.getId(effect);
        if (baseId == null) throw new IllegalStateException("Unregistered status effect " + effect);
        Identifier[] altSprites = new Identifier[altSpriteCount];
        for (int amplifier = 0; amplifier < altSpriteCount; amplifier++) {
            altSprites[amplifier] = new Identifier(baseId.getNamespace(), "mob_effect/" + baseId.getPath() + '_' + (amplifier + 1));
        }
        spriteMappings.put(new Identifier(baseId.getNamespace(), "mob_effect/" + baseId.getPath()), altSprites);
    }

    public Sprite substituteSprite(Sprite baseSprite, StatusEffectInstance renderedEffect) {
        Identifier[] altSprites = spriteMappings.get(baseSprite.getId());
        if (altSprites != null) {
            int amplifier = renderedEffect.getAmplifier();
            return ((SpriteAtlasHolderAccessor) MinecraftClient.getInstance().getStatusEffectSpriteManager())
                .requiem$getAtlas().getSprite(altSprites[Math.min(altSprites.length - 1, amplifier)]);
        }
        return baseSprite;
    }

    public void registerCallbacks() {
        // Register special icons for different levels of attrition
        ClientSpriteRegistryCallback.event(new Identifier("textures/atlas/mob_effects.png")).register(this);
    }

    @Override
    public void registerSprites(SpriteAtlasTexture atlasTexture, ClientSpriteRegistryCallback.Registry registry) {
        for (Identifier[] altSprites : spriteMappings.values()) {
            for (Identifier altSprite : altSprites) {
                registry.register(altSprite);
            }
        }
    }
}
