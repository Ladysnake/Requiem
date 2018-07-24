package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockPurificationCauldron;
import net.minecraftforge.fml.common.registry.GameRegistry;

@AutoRegister(Reference.MOD_ID)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public final class ModBlocks {

    public static final BlockPurificationCauldron PURIFICATION_CAULDRON = new BlockPurificationCauldron();

    private ModBlocks() {
    }
}
