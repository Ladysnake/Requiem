package ladysnake.dissolution.common.entity.souls;

import elucent.albedo.lighting.Light;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModPotions;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class EntityFaerie extends EntityFleetingSoul {

    private static final DataParameter<Boolean> TIRED = EntityDataManager.createKey(EntityFaerie.class, DataSerializers.BOOLEAN);


    public EntityFaerie(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(rand.nextInt(1000) == 0)
            this.setTired(false);
    }

    @Override
    protected void selectBlockTarget() {
        this.xTarget = this.posX + rand.nextGaussian() * 10;
        this.yTarget = this.posY + rand.nextGaussian() * 10;
        this.zTarget = this.posZ + rand.nextGaussian() * 10;
        targetChangeCooldown = rand.nextInt() % 200;
    }

    @Override
    protected Entity selectTarget() {
        return this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 32.0,
                player -> player instanceof EntityPlayer && !((EntityPlayer) player).isSpectator()
                        && CapabilityIncorporealHandler.getHandler(player).getPossessed() != null);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if(!this.isTired()) {
            entityIn.addPotionEffect(new PotionEffect(ModPotions.PURIFICATION, 200));
            this.setTired(true);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(TIRED, false);
    }

    public Boolean isTired() {
        return this.getDataManager().get(TIRED);
    }

    public void setTired(boolean tired) {
        this.getDataManager().set(TIRED, tired);
    }

    @Override
    public Light provideLight() {
        return Light.builder().pos(this).radius(this.isTired() ? 2 : 5).color(0.9f, 0.5f, 0.8f).build();
    }
}
