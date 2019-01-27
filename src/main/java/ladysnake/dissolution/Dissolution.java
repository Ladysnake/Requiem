package ladysnake.dissolution;

import ladysnake.dissolution.common.block.DissolutionBlocks;
import ladysnake.dissolution.common.entity.DissolutionEntities;
import ladysnake.dissolution.common.impl.movement.MovementAltererManager;
import ladysnake.dissolution.common.impl.possession.Possession;
import ladysnake.dissolution.common.impl.remnant.MutableRemnantState;
import ladysnake.dissolution.common.item.DissolutionItems;
import ladysnake.dissolution.common.network.ServerMessageHandling;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.ServerEvent;
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
        DissolutionBlocks.init();
        DissolutionEntities.init();
        DissolutionItems.init();
        MutableRemnantState.init();
        Possession.init();
        ServerMessageHandling.init();
        ServerEvent.START.register(server -> server.getDataManager().addListener(MOVEMENT_ALTERER_MANAGER));
    }
}
