package ladysnake.requiem.common.tag;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;

public final class RequiemBlockTags {
    public static final Tag<Block> SOUL_IMPERMEABLE = TagRegistry.block(Requiem.id("soul_impermeable"));
}
