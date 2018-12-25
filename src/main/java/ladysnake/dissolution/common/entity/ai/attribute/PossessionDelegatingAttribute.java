package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class PossessionDelegatingAttribute extends DelegatingAttribute {
    private IIncorporealHandler handler;

    public PossessionDelegatingAttribute(AbstractAttributeMap map, IAttributeInstance original, IIncorporealHandler handler) {
        super(map, original);
        this.handler = handler;
    }

    /**
     * @return the attribute instance to which calls should be delegated
     */
    @Override
    protected IAttributeInstance getDelegateAttributeInstance() {
        if (handler.isPossessionActive()) {
            EntityLivingBase possessed = handler.getPossessed();
            if (possessed != null) {
                IAttributeInstance ret = possessed.getEntityAttribute(this.getAttribute());
                // the attribute can be null if it is not registered in the possessed entity
                //noinspection ConstantConditions
                if (ret != null) {
                    return ret;
                }
            }
        }
        return super.getDelegateAttributeInstance();
    }
}
