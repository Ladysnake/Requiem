package ladysnake.requiem.common.remnant;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import ladysnake.requiem.mixin.entity.mob.EndermanEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.dimension.DimensionType;

public class BasePossessionHandlers {

    public static void register() {
        PossessionStartCallback.EVENT.register(Requiem.id("blacklist"), (target, possessor) -> {
            if (!target.world.isClient && RequiemEntityTags.POSSESSION_BLACKLIST.contains(target.getType())) {
                return PossessionStartCallback.Result.DENY;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionStartCallback.EVENT.register(Requiem.id("base_mobs"), (target, possessor) -> {
            if (!target.world.isClient && target.isUndead() || target instanceof GolemEntity) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionStartCallback.EVENT.register(Requiem.id("enderman"), BasePossessionHandlers::handleEndermanPossession);
        PossessionStartCallback.EVENT.register(Requiem.id("shulker"), (target, possessor) -> {
            if (target instanceof ShulkerEntity && target.world.isClient) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.inGameHud.setOverlayMessage(I18n.translate("requiem:shulker.onboard", client.options.keySneak.getLocalizedName()), false);
            }
            return PossessionStartCallback.Result.PASS;
        });
    }

    private static PossessionStartCallback.Result handleEndermanPossession(MobEntity target, PlayerEntity possessor) {
        if (!target.world.isClient && target instanceof EndermanEntity) {
            Entity tpDest;
            if (possessor.world.dimension.getType() == DimensionType.OVERWORLD) {
                // Retry a few times
                for (int i = 0; i < 20; i++) {
                    if (((EndermanEntityAccessor) target).invokeTeleportRandomly()) {
                        possessor.world.playSound(null, target.x, target.y, target.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, target.getSoundCategory(), 1.0F, 1.0F);
                        break;
                    }
                }
                tpDest = target;
            } else {
                possessor.world.playSound(null, target.x, target.y, target.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, target.getSoundCategory(), 1.0F, 1.0F);
                possessor.changeDimension(DimensionType.OVERWORLD);
                tpDest = target.changeDimension(DimensionType.OVERWORLD);
            }
            if (tpDest != null) {
                possessor.teleport(tpDest.x, tpDest.y, tpDest.z, true);
            }
            return PossessionStartCallback.Result.HANDLED;
        }
        return PossessionStartCallback.Result.PASS;
    }
}
