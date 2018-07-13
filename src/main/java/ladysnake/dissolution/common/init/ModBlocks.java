package ladysnake.dissolution.common.init;

import ladylib.registration.AutoRegister;
import ladysnake.dissolution.common.Reference;
import net.minecraftforge.fml.common.registry.GameRegistry;

@AutoRegister(Reference.MOD_ID)
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public final class ModBlocks {

    private ModBlocks() {
    }
}
