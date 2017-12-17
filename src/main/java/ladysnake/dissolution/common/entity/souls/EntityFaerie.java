package ladysnake.dissolution.common.entity.souls;

import elucent.albedo.lighting.Light;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class EntityFaerie extends EntityFleetingSoul {
    public EntityFaerie(World worldIn) {
        super(worldIn);
    }

    @Override
    protected Entity selectTarget() {
        return this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 32.0,
                player -> player instanceof EntityPlayer && !((EntityPlayer) player).isSpectator()
                        && CapabilityIncorporealHandler.getHandler(player).getPossessed() != null);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(entityIn);
        if(handler.getPossessed() != null && !entityIn.world.isRemote) {
            handler.setCorporealityStatus(CorporealityStatus.BODY);
        }
    }

    @Override
    public Light provideLight() {
        return Light.builder().pos(this).radius(5).color(0.9f, 0.5f, 0.8f).build();
    }
}
