package ladysnake.dissolution.client.gui.hud;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;

public class PossessionHud {
    public static final PossessionHud INSTANCE = new PossessionHud();

    public ActionResult onRenderHotbar(@SuppressWarnings("unused") float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        DissolutionPlayer player = (DissolutionPlayer) client.player;
        if (!client.player.isCreative() && player.getRemnantState().isSoul()) {
            Entity possessed = (Entity) player.getPossessionComponent().getPossessedEntity();
            if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }


}
