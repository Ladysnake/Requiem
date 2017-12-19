package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.souls.EntityFaerie;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

public enum SoulType {
    NONE("", world -> null),
    WILL_O_WISP("textures/entity/will_o_wisp.png", EntityFleetingSoul::new),
    FAERIE("textures/entity/faerie.png", EntityFaerie::new),
    TIRED_FAERIE("textures/entity/faerie_weak.png", world -> {
        EntityFaerie ret = new EntityFaerie(world);
        ret.setTired(true);
        return ret;
    });

    public final ResourceLocation texture;
    private Function<World, EntityFleetingSoul> factory;

    SoulType(String texture, Function<World, EntityFleetingSoul> factory) {
        this.texture = new ResourceLocation(Reference.MOD_ID, texture);
        this.factory = factory;
    }

    public Optional<EntityFleetingSoul> instantiate(World world, double x, double y, double z) {
        Optional<EntityFleetingSoul> ret = Optional.ofNullable(this.factory.apply(world));
        ret.ifPresent(soul -> soul.setPosition(x, y, z));
        return ret;
    }

}
