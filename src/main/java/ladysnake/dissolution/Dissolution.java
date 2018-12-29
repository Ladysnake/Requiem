package ladysnake.dissolution;

import ladysnake.dissolution.init.DissolutionBlocks;
import ladysnake.dissolution.init.DissolutionItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dissolution implements ModInitializer {
	public static final String MODID = "dissolution";
	public static final Logger LOGGER = LogManager.getLogger("Dissolution");

	@Override
	public void onInitialize() {
		DissolutionBlocks.init();
		DissolutionItems.init();

		PlayerInteractionEvent.ATTACK_BLOCK.register((player, world, hand, blockPos, facing) -> {
			if (!player.isCreative()) {
				Material blockMat = world.getBlockState(blockPos).getMaterial();
				if (blockMat == Material.BAMBOO || blockMat == Material.LEAVES || blockMat == Material.TNT || world.getBlockState(blockPos).getBlock() == Blocks.CLAY) {
					return ActionResult.PASS;
				} else if (world.getBlockState(blockPos).getBlock().getTranslationKey().contains("ore")) {
					return ActionResult.PASS;
				}

				return ActionResult.FAILURE;
			} else return ActionResult.PASS;
		});

	}
}
