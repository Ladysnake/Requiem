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
package ladysnake.requiem.compat;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.gamerule.StartingRemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class OriginsCompat {
    public static final SerializableDataType<RemnantType> REMNANT_TYPE = SerializableDataType.registry(RemnantType.class, RequiemRegistries.REMNANT_STATES);

    // The factory for the origins power that lets player decide whether they want to be a remnant or not
    public static final Identifier FACTORY_ID = Requiem.id("soul_type");
    public static final PowerFactory<OriginsRemnantPower> REMNANT_POWER_FACTORY = new PowerFactory<>(
        FACTORY_ID,
        new SerializableData().add("value", REMNANT_TYPE),
        instance -> {
            RemnantType remnantType = ((RemnantType) instance.get("value"));
            return (type, player) -> new OriginsRemnantPower(type, player, remnantType);
        }
    );

    // The factory for the origins player condition that locks access based on the requiem:startingRemnantType gamerule
    public static final Identifier GAMERULE_CONDITION_ID = Requiem.id("start_remnant_gamerule");
    public static final ConditionFactory<PlayerEntity> GAMERULE_CONDITION_FACTORY = new ConditionFactory<>(
        GAMERULE_CONDITION_ID,
        new SerializableData().add("value", SerializableDataType.list(SerializableDataType.enumValue(StartingRemnantType.class)), Collections.singletonList(StartingRemnantType.CHOOSE)),
        (instance, player) -> {
            StartingRemnantType startingRemnantType = StartingRemnantType.of(RemnantComponent.get(player).getDefaultRemnantType());
            return ((List<?>) instance.get("value")).contains(startingRemnantType);
        }
    );

    private static final Identifier SOUL_TYPE_LAYER_ID = Requiem.id("soul_type");
    private static Origin vagrant;

    private static void applyVagrantOrigin(PlayerEntity player) {
        if (vagrant != null) {
            OriginComponent originComponent = ModComponents.ORIGIN.get(player);
            for (OriginLayer originLayer : new HashSet<>(originComponent.getOrigins().keySet())) {
                if (!SOUL_TYPE_LAYER_ID.equals(originLayer.getIdentifier())) {
                    originComponent.setOrigin(originLayer, vagrant);
                }
            }
        }
    }

    public static void init() {
        Registry.register(ModRegistries.POWER_FACTORY, FACTORY_ID, REMNANT_POWER_FACTORY);
        Registry.register(ModRegistries.PLAYER_CONDITION, GAMERULE_CONDITION_ID, GAMERULE_CONDITION_FACTORY);
        RemnantStateChangeCallback.EVENT.register((player, state) -> {
            if (!player.world.isClient) {
                if (state.isSoul()) {
                    OriginHolder.KEY.get(player).storeOrigin(player);
                    applyVagrantOrigin(player);
                } else {
                    OriginHolder.KEY.get(player).restoreOrigin(player);
                }
            }
        });
        try {
            OriginDataLoadedCallback.EVENT.register(isClient -> {
                vagrant = OriginRegistry.get(Requiem.id("vagrant"));
                if (vagrant == null) throw new IllegalStateException("Special vagrant origin not found");
                vagrant.setSpecial();
            });
        } catch (NoClassDefFoundError e) {
            Requiem.LOGGER.error("[Requiem] Failed to register special Vagrant origin, consider updating Origins");
        }
    }
}
