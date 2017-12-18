package ladysnake.dissolution.common.potion;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

public class PotionPurification extends Potion {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/icons.png");

    public PotionPurification() {
        super(false, 0xCD5CAB);
        this.setIconIndex(0, 198);
    }

    @Override
    public void performEffect(@Nonnull EntityLivingBase entityLivingBaseIn, int amplifier) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(entityLivingBaseIn);
        if (handler == null) return;
        if (handler.getPossessed() != null) {
            handler.getPossessedStats().purifyHealth((amplifier + 1)*2);
        } else {
            Objects.requireNonNull(Potion.getPotionFromResourceLocation("minecraft:regeneration")).performEffect(entityLivingBaseIn, amplifier);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getStatusIconIndex() {
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        return super.getStatusIconIndex();
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int k = 50 >> amplifier;
        return k <= 0 || duration % k == 0;
    }
}
