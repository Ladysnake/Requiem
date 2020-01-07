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
package ladysnake.requiem;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.event.minecraft.SyncServerResourcesCallback;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.common.RequiemComponents;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.command.RequiemCommand;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.impl.ApiInitializer;
import ladysnake.requiem.common.impl.movement.MovementAltererManager;
import ladysnake.requiem.common.impl.remnant.dialogue.ReloadableDialogueRegistry;
import ladysnake.requiem.common.impl.resurrection.ResurrectionDataLoader;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.network.ServerMessageHandling;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
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

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    private ReloadableDialogueRegistry dialogueManager;

    @Override
    public void onInitialize() {
        ApiInitializer.init();
        RequiemCriteria.init();
        RequiemBlocks.init();
        RequiemEnchantments.init();
        RequiemItems.init();
        RequiemRegistries.init();
        RequiemSoundEvents.init();
        ServerMessageHandling.init();
        ApiInitializer.discoverEntryPoints();
        CommandRegistry.INSTANCE.register(false, RequiemCommand::register);
        this.registerSubDataManagers();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ResurrectionDataLoader.INSTANCE);
        RequiemComponents.initComponents();
        SyncServerResourcesCallback.EVENT.register(player -> RequiemNetworking.sendTo(player, RequiemNetworking.createDataSyncMessage(SubDataManagerHelper.getServerHelper())));
        ApiInitializer.setPluginCallback(this::registerPlugin);
    }

    private void registerSubDataManagers() {
        // Dialogues
        ReloadableDialogueRegistry serverDialogueManager = new ReloadableDialogueRegistry();
        ReloadableDialogueRegistry clientDialogueManager = new ReloadableDialogueRegistry();
        SubDataManagerHelper.getServerHelper().registerSubDataManager(serverDialogueManager);
        SubDataManagerHelper.getClientHelper().registerSubDataManager(clientDialogueManager);
        this.dialogueManager = serverDialogueManager;
        // Movement alterers
        MovementAltererManager serverMovementAltererManager = new MovementAltererManager();
        MovementAltererManager clientMovementAltererManager = new MovementAltererManager();
        SubDataManagerHelper.getServerHelper().registerSubDataManager(serverMovementAltererManager);
        SubDataManagerHelper.getClientHelper().registerSubDataManager(clientMovementAltererManager);
        // Access through World objects, similar to TagManager
        WorldComponentCallback.EVENT.register((world, components) -> {
            components.put(RequiemComponents.DIALOGUES, world.isClient ? clientDialogueManager : serverDialogueManager);
            components.put(RequiemComponents.MOVEMENT_ALTERERS, world.isClient ? clientMovementAltererManager : serverMovementAltererManager);
        });
    }

    private void registerPlugin(RequiemPlugin plugin) {
        plugin.onRequiemInitialize();
        plugin.registerRemnantStates(RequiemRegistries.REMNANT_STATES);
        plugin.registerMobAbilities(RequiemRegistries.ABILITIES);
        plugin.registerDialogueActions(dialogueManager);
    }
}
