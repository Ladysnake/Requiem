package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.blocks.BlockPurificationCauldron;
import net.minecraftforge.fml.common.registry.GameRegistry;

@AutoRegister(Ref.MOD_ID)
@GameRegistry.ObjectHolder(Ref.MOD_ID)
public final class ModBlocks {

    @AutoRegister.NoItem
    public static final BlockPurificationCauldron PURIFICATION_CAULDRON = new BlockPurificationCauldron();

    private ModBlocks() {
    }
}
