/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.client;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.client.network.ClientMessageHandler;
import ladysnake.requiem.client.particle.CureParticle;
import ladysnake.requiem.client.particle.GhostParticle;
import ladysnake.requiem.client.render.entity.CuredPiglinEntityRenderer;
import ladysnake.requiem.client.render.entity.CuredVillagerEntityRenderer;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class RequiemClient {

    public static final Identifier CRAFTING_BUTTON_TEXTURE = Requiem.id("textures/gui/crafting_button.png");

    private static final RequiemClient INSTANCE = new RequiemClient();

    @CalledThroughReflection
    public static void onInitializeClient() {
        INSTANCE.init();
    }

    public static RequiemClient instance() {
        return INSTANCE;
    }

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final ClientMessageHandler messageHandler;
    private final RequiemClientListener listener;
    private final RequiemTargetHandler targetHandler;
    private final RequiemEntityShaderPicker shaderPicker;
    private final RequiemStatusEffectSpriteManager statusEffectSpriteManager;

    private final RequiemFx requiemFxRenderer;
    private final ShadowPlayerFx shadowPlayerFxRenderer;
    private final ZaWorldFx worldFreezeFxRenderer;

    private RequiemClient() {
        this.messageHandler = new ClientMessageHandler(this);
        this.listener = new RequiemClientListener(this);
        this.targetHandler = new RequiemTargetHandler();
        this.requiemFxRenderer = new RequiemFx();
        this.shaderPicker = new RequiemEntityShaderPicker();
        this.statusEffectSpriteManager = new RequiemStatusEffectSpriteManager();
        this.shadowPlayerFxRenderer = new ShadowPlayerFx();
        this.worldFreezeFxRenderer = new ZaWorldFx();
    }

    public void updateCamera(PlayerEntity player, Entity cameraEntity) {
        if (this.mc.options.getPerspective().isFirstPerson() && player == this.mc.player) {
            this.mc.gameRenderer.onCameraEntitySet(cameraEntity);
        }
    }

    public RequiemStatusEffectSpriteManager statusEffectSpriteManager() {
        return statusEffectSpriteManager;
    }

    public ShadowPlayerFx shadowPlayerFxRenderer() {
        return shadowPlayerFxRenderer;
    }

    public ZaWorldFx worldFreezeFxRenderer() {
        return worldFreezeFxRenderer;
    }

    public RequiemFx fxRenderer() {
        return requiemFxRenderer;
    }

    private void init() {
        this.registerEntityRenderers();
        this.registerModelPredicates();
        this.registerParticleFactories();
        this.registerSprites();
        this.initListeners();
        FractureKeyBinding.init();
    }

    private void registerParticleFactories() {
        ParticleFactoryRegistry.getInstance().register(RequiemParticleTypes.GHOST, GhostParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(RequiemParticleTypes.ATTRITION, PortalParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(RequiemParticleTypes.CURE, CureParticle.Factory::new);
    }

    private void registerModelPredicates() {
    }

    private void registerSprites() {
        this.statusEffectSpriteManager().registerAltSprites(RequiemStatusEffects.ATTRITION, 4);
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.INSTANCE.register(RequiemEntities.CURED_VILLAGER, CuredVillagerEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(RequiemEntities.CURED_PIGLIN, (ctx) -> new CuredPiglinEntityRenderer(ctx, EntityModelLayers.PIGLIN, EntityModelLayers.PIGLIN_INNER_ARMOR, EntityModelLayers.PIGLIN_OUTER_ARMOR, false));
        EntityRendererRegistry.INSTANCE.register(RequiemEntities.CURED_PIGLIN_BRUTE, (ctx) -> new CuredPiglinEntityRenderer(ctx, EntityModelLayers.PIGLIN_BRUTE, EntityModelLayers.PIGLIN_BRUTE_INNER_ARMOR, EntityModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false));
    }

    private void initListeners() {
        this.messageHandler.init();
        this.shaderPicker.registerCallbacks();
        this.requiemFxRenderer.registerCallbacks();
        this.shadowPlayerFxRenderer.registerCallbacks();
        this.worldFreezeFxRenderer.registerCallbacks();
        this.listener.registerCallbacks();
        this.targetHandler.registerCallbacks();
        this.statusEffectSpriteManager.registerCallbacks();
    }
}
