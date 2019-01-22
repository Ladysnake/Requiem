package ladysnake.dissolution.common.tag;

import ladysnake.dissolution.Dissolution;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.EntityTags;
import net.minecraft.tag.Tag;

public final class DissolutionEntityTags {
    public static final Tag<EntityType<?>> POSSESSION_BLACKLIST = register("possession_blacklist");
    public static final Tag<EntityType<?>> ITEM_USER = register("item_user");
    public static final Tag<EntityType<?>> FLIGHT = register("flight");
    public static final Tag<EntityType<?>> CLIMBER = register("climber");

    public static Tag<EntityType<?>> register(String name) {
        return new EntityTags.class_3484(Dissolution.id(name));
    }

}
