package ladysnake.requiem.common.remnant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

public class MortalDysmorphiaDamageSource extends DamageSource {
    private final Text soulName;
    private final Text bodyName;

    public MortalDysmorphiaDamageSource(Text soulName, Text bodyName) {
        super("requiem.mortal_dysmorphia");
        this.soulName = soulName;
        this.bodyName = bodyName;
        this.setUsesMagic();
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        return Text.translatable("death.attack." + this.name, this.soulName, this.bodyName);
    }
}
