package ladysnake.requiem.common.tag;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;

public final class RequiemFluidTags {
    public static final Tag<Fluid> EMPTY = TagRegistry.fluid(Requiem.id("empty"));
}
