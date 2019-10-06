package ladysnake.pandemonium.mixin.common.special.soulweb;

import io.netty.buffer.Unpooled;
import ladysnake.pandemonium.common.entity.effect.PandemoniumStatusEffects;
import ladysnake.pandemonium.common.network.PandemoniumNetworking;
import ladysnake.pandemonium.common.util.PathNetworkSerializer;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.Packet;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    public abstract EntityNavigation getNavigation();

    @Unique
    private Path lastPath;
    @Unique
    private int lastNodeIndex;
    protected MobEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickNewAi", at = @At("RETURN"))
    private void sendPath(CallbackInfo ci) {
        if (this.hasStatusEffect(PandemoniumStatusEffects.SOUL_WEBBED)) {
            Path path = this.getNavigation().getCurrentPath();
            if (path != null && (!path.equalsPath(this.lastPath) || path.getCurrentNodeIndex() != lastNodeIndex)) {
                Packet<?> packet = ServerSidePacketRegistry.INSTANCE.toPacket(
                    PandemoniumNetworking.SOUL_WEB_PATH,
                    PathNetworkSerializer.serializePath(
                        path,
                        this.getEntityId(),
                        new PacketByteBuf(Unpooled.buffer())
                    )
                );
                PlayerStream.watching(this)
                    .filter(p -> RequiemPlayer.from(p).asRemnant().isIncorporeal())
                    .forEach(p -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, packet));
                this.lastPath = path;
            }
        }
    }
}
