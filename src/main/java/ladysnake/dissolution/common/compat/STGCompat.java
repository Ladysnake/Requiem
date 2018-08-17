package ladysnake.dissolution.common.compat;

import ladylib.compat.EnhancedBusSubscriber;
import ladylib.compat.StateEventReceiver;
import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.util.function.Function;

/**
 * Swing Through Grass compatibility: prevent players from hitting the entity they are possessing
 */
@EnhancedBusSubscriber("stg")
public class STGCompat implements StateEventReceiver, Function<EntityLivingBase, Boolean> {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        FMLInterModComms.sendFunctionMessage("stg", "", STGCompat.class.getName());
    }

    /**
     * This has the side effect of preventing other players from hitting possessed mobs through tall grass
     * but lesser evil and all that
     */
    @Override
    public Boolean apply(EntityLivingBase entityLivingBase) {
        return !(entityLivingBase instanceof IPossessable && ((IPossessable) entityLivingBase).getPossessingEntityId() != null);
    }
}
