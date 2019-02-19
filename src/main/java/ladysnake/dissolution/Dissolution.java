package ladysnake.dissolution;

import ladysnake.dissolution.api.v1.DissolutionApi;
import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.common.DissolutionRegistries;
import ladysnake.dissolution.common.VanillaDissolutionPlugin;
import ladysnake.dissolution.common.block.DissolutionBlocks;
import ladysnake.dissolution.common.command.DissolutionCommand;
import ladysnake.dissolution.common.entity.DissolutionEntities;
import ladysnake.dissolution.common.impl.ApiInitializer;
import ladysnake.dissolution.common.impl.movement.MovementAltererManager;
import ladysnake.dissolution.common.item.DissolutionItems;
import ladysnake.dissolution.common.network.ServerMessageHandling;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dissolution implements ModInitializer {
    public static final String MOD_ID = "dissolution";
    public static final Logger LOGGER = LogManager.getLogger("Dissolution");

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
        DissolutionBlocks.init();
        DissolutionEntities.init();
        DissolutionItems.init();
        DissolutionRegistries.init();
        ServerMessageHandling.init();
        DissolutionApi.registerPlugin(new VanillaDissolutionPlugin());
        CommandRegistry.INSTANCE.register(false, DissolutionCommand::register);
        ResourceManagerHelper.get(ResourceType.DATA).registerReloadListener(MOVEMENT_ALTERER_MANAGER);
        ApiInitializer.setPluginCallback(Dissolution::registerPlugin);
    }

    private static void registerPlugin(DissolutionPlugin plugin) {
        plugin.onDissolutionInitialize();
        plugin.registerRemnantStates(DissolutionRegistries.REMNANT_STATES);
        plugin.registerMobAbilities(DissolutionRegistries.ABILITIES);
        plugin.registerPossessedConversions(DissolutionRegistries.CONVERSION);
    }
}
