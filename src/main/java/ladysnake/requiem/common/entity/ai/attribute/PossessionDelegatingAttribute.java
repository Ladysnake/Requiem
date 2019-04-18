package ladysnake.requiem.common.entity.ai.attribute;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;

public class PossessionDelegatingAttribute extends DelegatingAttribute {
    private final PossessionComponent handler;

    public PossessionDelegatingAttribute(AbstractEntityAttributeContainer map, EntityAttributeInstance original, PossessionComponent handler) {
        super(map, original);
        this.handler = handler;
    }

    /**
     * @return the attribute instance to which calls should be delegated
     */
    @Override
    protected EntityAttributeInstance getDelegateAttributeInstance() {
        if (handler.isPossessing()) {
            LivingEntity possessed = (LivingEntity) handler.getPossessedEntity();
            if (possessed != null) {
                EntityAttributeInstance ret = possessed.getAttributeInstance(this.getAttribute());
                // the attribute can be null if it is not registered in the possessed entity
                if (ret != null) {
                    return ret;
                }
            }
        }
        return super.getDelegateAttributeInstance();
    }
}
