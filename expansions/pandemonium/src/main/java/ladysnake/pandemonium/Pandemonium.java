/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.pandemonium;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import io.github.ladysnake.impersonate.Impersonate;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.pandemonium.client.ClientAnchorManager;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.entity.WololoComponent;
import ladysnake.pandemonium.common.impl.anchor.CommonAnchorManager;
import ladysnake.pandemonium.common.network.ServerMessageHandling;
import ladysnake.pandemonium.common.remnant.PlayerBodyTracker;
import ladysnake.pandemonium.compat.PandemoniumCompatibilityManager;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

@CalledThroughReflection
public class Pandemonium implements ModInitializer, EntityComponentInitializer, WorldComponentInitializer {
    public static final String MOD_ID = "pandemonium";
    public static final Identifier BODY_IMPERSONATION = Requiem.id("body_impersonation");
    public static final Pandemonium INSTANCE = new Pandemonium();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static void tickAnchors(World world) {
        Profiler profiler = world.getProfiler();
        profiler.push("requiem_ethereal_anchors");
        FractureAnchorManager.get(world).updateAnchors(world.getLevelProperties().getTime());
        profiler.pop();
    }

    @Override
    public void onInitialize() {
        PandemoniumEntities.init();
        ServerMessageHandling.init();
        RequiemApi.registerPlugin(new PandemoniumRequiemPlugin());
        ServerTickEvents.END_WORLD_TICK.register(Pandemonium::tickAnchors);
        PlayerRespawnCallback.EVENT.register((player, returnFromEnd) -> {
            if (!returnFromEnd) Impersonate.IMPERSONATION.get(player).stopImpersonation(BODY_IMPERSONATION);
        });
        RemnantStateChangeCallback.EVENT.register((player, state) -> {
            if (state.isVagrant()) Impersonate.IMPERSONATION.get(player).stopImpersonation(BODY_IMPERSONATION);
        });
        PandemoniumCompatibilityManager.init();
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PlayerBodyTracker.KEY, PlayerBodyTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerFor(EndermanEntity.class, WololoComponent.KEY, WololoComponent::create);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(FractureAnchorManager.KEY, world -> world.isClient
            ? new ClientAnchorManager(world)
            : new CommonAnchorManager(world)
        );
    }

    private Pandemonium() {
        super();
    }
}
