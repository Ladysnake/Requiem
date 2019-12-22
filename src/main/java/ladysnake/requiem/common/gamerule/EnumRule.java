/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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

import com.mojang.brigadier.context.CommandContext;
import ladysnake.requiem.Requiem;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

public final class EnumRule<E extends Enum<E>> extends GameRules.Rule<EnumRule<E>> {
    private final Class<E> enumType;
    private E value;

    EnumRule(GameRules.RuleType<EnumRule<E>> ruleType, Class<E> enumType, E value) {
        super(ruleType);
        this.enumType = enumType;
        this.value = value;
    }

    protected void setFromArgument(CommandContext<ServerCommandSource> commandContext, String name) {
        this.value = commandContext.getArgument(name, this.enumType);
    }

    public E get() {
        return this.value;
    }

    @Override
    protected String serialize() {
        return this.value.toString();
    }

    @Override
    protected void deserialize(String value) {
        if (!value.isEmpty()) {
            try {
                this.value = Enum.valueOf(this.enumType, value);
            } catch (IllegalArgumentException e) {
                Requiem.LOGGER.warn("[Requiem] Failed to parse enum {} for {}", value, this.enumType.getName());
                this.value = this.enumType.getEnumConstants()[0];
            }
        }
    }

    @Override
    public int getCommandResult() {
        return this.value.ordinal();
    }

    @Override
    protected EnumRule<E> getThis() {
        return this;
    }
}
