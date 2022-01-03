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
package ladysnake.requiem.compat;

import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.core.entity.ability.RangedAttackAbility;
import ladysnake.snowmercy.common.SnowMercy;
import ladysnake.snowmercy.common.entity.ChillSnugglesEntity;
import ladysnake.snowmercy.common.entity.MortarsEntity;
import ladysnake.snowmercy.common.entity.RocketsEntity;
import ladysnake.snowmercy.common.entity.SnugglesEntity;
import net.minecraft.util.Identifier;

public final class SnowMercyCompat implements RequiemPlugin {
    @CalledThroughReflection
    public static void init() {
        RequiemApi.registerPlugin(new SnowMercyCompat());
    }

    private static Identifier id(String path) {
        return new Identifier(SnowMercy.MODID, path);
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry registry) {
        RequiemCompatibilityManager.<SnugglesEntity>findEntityType(id("mister_snuggles"), object ->
            registry.register(object, MobAbilityConfig.<SnugglesEntity>builder().directAttack(SnugglesBoomAbility::new).build()));
        RequiemCompatibilityManager.<ChillSnugglesEntity>findEntityType(id("mister_chill_snuggles"), object ->
            registry.register(object, MobAbilityConfig.<ChillSnugglesEntity>builder().directAttack(SnugglesBoomAbility::new).build()));
        RequiemCompatibilityManager.<RocketsEntity>findEntityType(id("aftermarket_snowman"), object ->
            registry.register(object, MobAbilityConfig.<RocketsEntity>builder().directAttack(e -> new RangedAttackAbility<>(e, 120, 32)).build()));
        RequiemCompatibilityManager.<MortarsEntity>findEntityType(id("ice_mortar"), object ->
            registry.register(object, MobAbilityConfig.<MortarsEntity>builder().directAttack(e -> new RangedAttackAbility<>(e, 80, 40)).build()));
    }
}
