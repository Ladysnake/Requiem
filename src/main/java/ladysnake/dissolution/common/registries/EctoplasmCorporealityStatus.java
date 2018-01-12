package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class EctoplasmCorporealityStatus extends IncorporealStatus {

    @Override
    public boolean preventsInteraction(IBlockState block) {
        return !DissolutionConfigManager.canEctoplasmInteractWith(block.getBlock());
    }
}
