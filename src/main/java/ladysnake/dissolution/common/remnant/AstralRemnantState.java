package ladysnake.dissolution.common.remnant;

import ladysnake.dissolution.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StringTextComponent;

public class AstralRemnantState extends FracturableRemnantState {
    public AstralRemnantState(RemnantType type, PlayerEntity owner) {
        super(type, owner);
    }

    @Override
    public void fracture() {
        if (this.isIncorporeal()) {
            this.project();
        } else {
            super.fracture();
        }
    }

    protected void project() {
        this.player.addChatMessage(new StringTextComponent("Projection!"), true);
    }
}
