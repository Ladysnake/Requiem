package ladysnake.pandemonium.common.entity;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.pandemonium.client.render.entity.ClientWololoComponent;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class WololoComponent implements AutoSyncedComponent {
    public static final ComponentKey<WololoComponent> KEY = ComponentRegistry.getOrCreate(Requiem.id("wololo"), WololoComponent.class);

    public static boolean canBeConverted(Entity entity) {
        WololoComponent w = KEY.getNullable(entity);
        return w != null && w.canBeConverted();
    }

    public static boolean isConverted(Entity entity) {
        WololoComponent w = KEY.getNullable(entity);
        return w != null && w.isConverted();
    }

    public static WololoComponent create(LivingEntity entity) {
        return entity.world.isClient ? new ClientWololoComponent(entity) : new WololoComponent(entity);
    }

    private final LivingEntity entity;
    private boolean converted = false;

    public WololoComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public boolean isConverted() {
        return converted;
    }

    public boolean canBeConverted() {
        return !isConverted();
    }

    public void wololo() {
        if (this.canBeConverted()) {
            this.converted = true;
            KEY.sync(this.entity);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.converted);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.converted = buf.readBoolean();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("converted")) {
            this.converted = tag.getBoolean("converted");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if (this.converted) {
            tag.putBoolean("converted", true);
        }
    }
}
