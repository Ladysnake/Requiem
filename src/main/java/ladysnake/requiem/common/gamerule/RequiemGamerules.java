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
 */
package ladysnake.requiem.common.gamerule;

import ladysnake.requiem.Requiem;
import net.minecraft.world.GameRules;

public class RequiemGamerules {
    public static final GameRules.RuleKey<GameRules.BooleanRule> SHOW_POSSESSOR_NAMETAG =
        register("showPossessorNameTag", GameruleHelper.createBooleanRule(false));
    public static final GameRules.RuleKey<GameRules.BooleanRule> SPAWN_HELP_ENDERMEN =
        register("spawn_help_endermen", GameruleHelper.createBooleanRule(true));
    public static final GameRules.RuleKey<EnumRule<StartingRemnantType>> STARTING_SOUL_MODE =
        register("startingRemnantType", GameruleHelper.createEnumRule(StartingRemnantType.CHOOSE));

    public static void init() {
        // static init
    }

    private static <T extends GameRules.Rule<T>> GameRules.RuleKey<T> register(String name, GameRules.RuleType<T> type) {
        return GameruleHelper.register(Requiem.MOD_ID + ":" + name, type);
    }

}
