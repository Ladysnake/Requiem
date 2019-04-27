package ladysnake.requiem.common.tag;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class RequiemItemTags {
    public static final Tag<Item> BONES = TagRegistry.item(new Identifier("c:bones"));
    public static final Tag<Item> RAW_MEATS = TagRegistry.item(new Identifier("c:raw_meats"));
}
