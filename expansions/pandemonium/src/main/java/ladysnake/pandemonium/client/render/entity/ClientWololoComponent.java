package ladysnake.pandemonium.client.render.entity;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.common.entity.WololoComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import org.jetbrains.annotations.Nullable;

public class ClientWololoComponent extends WololoComponent {
    private static final RenderLayer CLASSIC_ENDERMAN_EYES = RenderLayer.getEyes(Pandemonium.id("textures/entity/enderman/classic_enderman_eyes.png"));

    private final @Nullable RenderLayer eyesLayer;

    public ClientWololoComponent(LivingEntity entity) {
        super(entity);
        if (entity instanceof EndermanEntity) {
            eyesLayer = CLASSIC_ENDERMAN_EYES;
        } else {
            eyesLayer = null;
        }
    }

    public @Nullable RenderLayer getEyesLayer() {
        return this.isConverted() ? eyesLayer : null;
    }
}
