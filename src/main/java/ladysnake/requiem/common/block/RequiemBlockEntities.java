package ladysnake.requiem.common.block;

import ladysnake.requiem.Requiem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public final class RequiemBlockEntities {
    public static final BlockEntityType<RunicObsidianBlockEntity> RUNIC_OBSIDIAN = BlockEntityType.Builder.create(RunicObsidianBlockEntity::new, RequiemBlocks.RUNIC_OBSIDIAN).build(null);

    public static void init() {
        register("runic_obsidian", RUNIC_OBSIDIAN);
    }

    private static void register(String id, BlockEntityType<?> type) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Requiem.id(id), type);
    }
}
