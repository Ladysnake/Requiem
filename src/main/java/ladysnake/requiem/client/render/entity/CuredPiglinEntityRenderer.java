package ladysnake.requiem.client.render.entity;

import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.RequiemEntities;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PiglinEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class CuredPiglinEntityRenderer extends PiglinEntityRenderer {
    private static final Map<EntityType<?>, Identifier> piglinTextures = ImmutableMap.of(
        RequiemEntities.CURED_PIGLIN, Requiem.id("textures/entity/cured_piglin.png"),
        RequiemEntities.CURED_PIGLIN_BRUTE, Requiem.id("textures/entity/cured_piglin_brute.png")
    );

    @Override
    public Identifier getTexture(MobEntity mobEntity) {
        Identifier identifier = piglinTextures.get(mobEntity.getType());
        if (identifier == null) {
            throw new IllegalArgumentException("I don't know what texture to use for " + mobEntity.getType());
        } else {
            return identifier;
        }
    }

    public CuredPiglinEntityRenderer(EntityRenderDispatcher dispatcher, boolean zombified) {
        super(dispatcher, zombified);
    }
}
