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
package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.minecraft.DynamicRegistryRegistrationCallback;
import ladysnake.requiem.api.v1.possession.item.PossessionItemAction;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.impl.possession.item.PossessionItemOverrideWrapper;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Entry point for the possession mechanic.
 * Everything here is subject to be moved to a more specialized place.
 */
@API(status = EXPERIMENTAL)
public final class RequiemRegistries {

    public static final SimpleRegistry<PossessionItemAction> MOB_ACTIONS =
        FabricRegistryBuilder.createSimple(PossessionItemAction.class, Requiem.id("mob_actions")).buildAndRegister();
    public static final DefaultedRegistry<RemnantType> REMNANT_STATES =
        FabricRegistryBuilder.createDefaulted(RemnantType.class, Requiem.id("remnant_states"), new Identifier(RemnantState.NULL_STATE_ID))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final RegistryKey<Registry<PossessionItemOverrideWrapper>> MOB_ITEM_OVERRIDE_KEY = RegistryKey.ofRegistry(Requiem.id("requiem/mob_items"));

    public static void init() {
        Registry.register(REMNANT_STATES, new Identifier(RemnantState.NULL_STATE_ID), RemnantTypes.MORTAL);

        RequiemBuiltinRegistries.init();

        DynamicRegistryRegistrationCallback.EVENT.register(helper -> helper.registerSynced(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY, PossessionItemOverrideWrapper.CODEC, PossessionItemOverrideWrapper.NETWORK_CODEC));
    }
}
