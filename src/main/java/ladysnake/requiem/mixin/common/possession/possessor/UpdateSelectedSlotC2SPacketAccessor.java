package ladysnake.requiem.mixin.common.possession.possessor;

import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateSelectedSlotC2SPacket.class)
public interface UpdateSelectedSlotC2SPacketAccessor {
    @Accessor
    void setSelectedSlot(int selectedSlot);
}
