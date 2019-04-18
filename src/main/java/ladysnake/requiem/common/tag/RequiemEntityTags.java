package ladysnake.requiem.common.tag;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.EntityTags;
import net.minecraft.tag.Tag;

public final class RequiemEntityTags {
    public static final Tag<EntityType<?>> POSSESSION_BLACKLIST = register("possession_blacklist");
    public static final Tag<EntityType<?>> ITEM_USER = register("item_user");
    public static final Tag<EntityType<?>> CLIMBER = register("climber");

    public static Tag<EntityType<?>> register(String name) {
        return new EntityTags.class_3484(Requiem.id(name));
    }

}
