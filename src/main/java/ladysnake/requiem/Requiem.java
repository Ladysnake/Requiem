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
package ladysnake.requiem;

import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.command.RequiemCommand;
import ladysnake.requiem.common.impl.ApiInitializer;
import ladysnake.requiem.common.impl.movement.MovementAltererManager;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.network.ServerMessageHandling;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Requiem implements ModInitializer {
    public static final String MOD_ID = "requiem";
    public static final Logger LOGGER = LogManager.getLogger("Requiem");

    private static final MovementAltererManager MOVEMENT_ALTERER_MANAGER = new MovementAltererManager();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static MovementAltererManager getMovementAltererManager() {
        return MOVEMENT_ALTERER_MANAGER;
    }

    @Override
    public void onInitialize() {
        ApiInitializer.init();
        RequiemCriteria.init();
        RequiemBlocks.init();
        RequiemItems.init();
        RequiemRegistries.init();
        ServerMessageHandling.init();
        RequiemApi.registerPlugin(new VanillaRequiemPlugin());
        CommandRegistry.INSTANCE.register(false, RequiemCommand::register);
        ResourceManagerHelper.get(ResourceType.DATA).registerReloadListener(MOVEMENT_ALTERER_MANAGER);
        ApiInitializer.setPluginCallback(Requiem::registerPlugin);
    }

    private static void registerPlugin(RequiemPlugin plugin) {
        plugin.onRequiemInitialize();
        plugin.registerRemnantStates(RequiemRegistries.REMNANT_STATES);
        plugin.registerMobAbilities(RequiemRegistries.ABILITIES);
    }
}
