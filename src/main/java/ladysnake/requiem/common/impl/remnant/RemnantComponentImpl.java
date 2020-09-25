package ladysnake.requiem.common.impl.remnant;

import dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class RemnantComponentImpl implements RemnantComponent, AutoSyncedComponent {
    private final PlayerEntity player;

    private RemnantState state = NullRemnantState.NULL_STATE;
    private RemnantType remnantType = RemnantTypes.MORTAL;

    public RemnantComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void become(RemnantType type) {
        if (type == this.remnantType) {
            return;
        }

        RemnantState handler = type.create(this.player);
        this.state.setSoul(false);
        this.state = handler;
        this.remnantType = type;
        RemnantComponent.KEY.sync(this.player);
    }

    @Override
    public RemnantType getRemnantType() {
        return this.remnantType;
    }

    @Override
    public boolean isIncorporeal() {
        return this.state.isIncorporeal();
    }

    @Override
    public boolean isSoul() {
        return this.state.isSoul();
    }

    @Override
    public void setSoul(boolean incorporeal) {
        this.state.setSoul(incorporeal);
    }

    @Override
    public void tick() {
        this.state.serverTick();
    }

    @Override
    public void copyFrom(ServerPlayerEntity original, boolean lossless) {
        this.state.copyFrom(original, lossless);
    }

    @Override
    public void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
        buf.writeVarInt(RemnantTypes.getRawId(this.remnantType));
        buf.writeBoolean(this.isSoul());
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        int remnantId = buf.readVarInt();
        boolean soul = buf.readBoolean();

        this.become(RemnantTypes.get(remnantId));
        this.setSoul(soul);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        RemnantType remnantType = RemnantTypes.get(new Identifier(compoundTag.getString("id")));
        this.become(remnantType);
        this.state.fromTag(compoundTag);
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putString("id", RemnantTypes.getId(this.remnantType).toString());
        this.state.toTag(compoundTag);
    }
}
