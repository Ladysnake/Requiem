/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.item;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.item.dispensing.RequiemDispenserBehaviors;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RequiemItems {
    public static final DebugItem DEBUG_ITEM = new DebugItem(new Item.Settings());
    public static final Item TOTEM_OF_SKELETONIZATION = new Item((new Item.Settings()).maxCount(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON));
    public static final EmptySoulVesselItem EMPTY_SOUL_VESSEL = new EmptySoulVesselItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final CreativeSoulVesselItem CREATIVE_SOUL_VESSEL = new CreativeSoulVesselItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final FilledSoulVesselItem FILLED_SOUL_VESSEL = new FilledSoulVesselItem(new Item.Settings().group(ItemGroup.MISC).recipeRemainder(EMPTY_SOUL_VESSEL).maxCount(1), EMPTY_SOUL_VESSEL);
    public static final Item SHATTERED_SOUL_VESSEL = new Item(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final DemonSoulVesselItem PURE_SOUL_VESSEL = new DemonSoulVesselItem(RemnantTypes.MORTAL, Formatting.AQUA, new Item.Settings().group(ItemGroup.MISC).maxCount(1), "requiem:remnant_vessel.cure");
    public static final DemonSoulVesselItem SEALED_REMNANT_VESSEL = new DemonSoulVesselItem(RemnantTypes.REMNANT, Formatting.RED, new Item.Settings().group(ItemGroup.MISC).maxCount(1), "requiem:remnant_vessel.curse");
    public static final Item MORTICIAN_SPAWN_EGG = new SpawnEggItem(RequiemEntities.MORTICIAN, 0x592a10, 0x494949, new Item.Settings().group(ItemGroup.MISC));

    public static final Map<StatusEffect, IchorVesselItem> vesselsByEffect = new LinkedHashMap<>();
    public static final IchorVesselItem ICHOR_VESSEL_ATTRITION = makeIchorVessel(RequiemStatusEffects.ATTRITION);
    public static final IchorVesselItem ICHOR_VESSEL_EMANCIPATION = makeIchorVessel(RequiemStatusEffects.EMANCIPATION);
    public static final IchorVesselItem ICHOR_VESSEL_PENANCE = makeIchorVessel(RequiemStatusEffects.PENANCE);
    public static final IchorVesselItem ICHOR_VESSEL_RECLAMATION = makeIchorVessel(RequiemStatusEffects.RECLAMATION);
    public static final int ICHOR_DEFAULT_DURATION = 20 * 60 * 10;

    private static IchorVesselItem makeIchorVessel(StatusEffect statusEffect) {
        IchorVesselItem item = new IchorVesselItem(new Item.Settings().group(ItemGroup.BREWING).maxCount(1), new StatusEffectInstance(statusEffect, ICHOR_DEFAULT_DURATION, 0, false, false, true));
        vesselsByEffect.put(statusEffect, item);
        return item;
    }

    public static void init() {
        registerItem(DEBUG_ITEM, "debug_item");
        registerItem(TOTEM_OF_SKELETONIZATION, "totem_of_skeletonization");
        registerItem(PURE_SOUL_VESSEL, "pure_soul_vessel");
        registerItem(EMPTY_SOUL_VESSEL, "empty_soul_vessel");
        registerItem(CREATIVE_SOUL_VESSEL, "creative_soul_vessel");
        registerItem(FILLED_SOUL_VESSEL, "filled_soul_vessel");
        registerItem(SHATTERED_SOUL_VESSEL, "shattered_soul_vessel");
        registerItem(SEALED_REMNANT_VESSEL, "sealed_remnant_vessel");
        registerItem(ICHOR_VESSEL_ATTRITION, "ichor_vessel_attrition");
        registerItem(ICHOR_VESSEL_EMANCIPATION, "ichor_vessel_emancipation");
        registerItem(ICHOR_VESSEL_PENANCE, "ichor_vessel_penance");
        registerItem(ICHOR_VESSEL_RECLAMATION, "ichor_vessel_reclamation");
        registerItem(MORTICIAN_SPAWN_EGG, "mortician_spawn_egg");

        RequiemDispenserBehaviors.registerDispenserBehaviors();

        FILLED_SOUL_VESSEL.registerCallbacks();
    }

    public static <T extends Item> void registerItem(T item, String name) {
        Registry.register(Registry.ITEM, Requiem.id(name), item);
    }
}
