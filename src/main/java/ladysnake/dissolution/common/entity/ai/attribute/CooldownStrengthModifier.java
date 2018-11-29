package ladysnake.dissolution.common.entity.ai.attribute;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;

public class CooldownStrengthModifier extends AttributeModifier {
    private final EntityPlayer player;

    public CooldownStrengthModifier(String nameIn, EntityPlayer player, int operationIn) {
        super(nameIn, 0, operationIn);
        this.player = player;
    }

    @Override
    public double getAmount() {
        return player.getCooledAttackStrength(0.5f) - 1;
    }

    @Override
    public boolean isSaved() {
        return false;
    }
}
