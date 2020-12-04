package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SoulPossessAbility extends DirectAbilityBase<PlayerEntity, MobEntity> {
    public static final Identifier POSSESSION_ICON = Requiem.id("textures/gui/possession_icon.png");

    public static final int POSSESSION_RANGE = 5;
    public static final int POSSESSION_COOLDOWN = 8;
    private final PlayerEntity player;

    private @Nullable MobEntity target;

    public SoulPossessAbility(PlayerEntity owner) {
        super(owner, POSSESSION_COOLDOWN, POSSESSION_RANGE, MobEntity.class);
        this.player = owner;
    }

    private PossessionComponent getPossessor() {
        return PossessionComponent.get(this.player);
    }

    @Override
    public boolean canTarget(MobEntity target) {
        return super.canTarget(target) && this.getPossessor().startPossessing(target, true);
    }

    @Override
    protected boolean run(MobEntity target) {
        this.target = target;
        if (this.owner.world.isClient && this.owner == MinecraftClient.getInstance().player) {
            RequiemClient.INSTANCE.getRequiemFxRenderer().beginFishEyeAnimation(target);
        }
        target.world.playSound(player, target.getX(), target.getY(), target.getZ(), RequiemSoundEvents.EFFECT_POSSESSION_ATTEMPT, SoundCategory.PLAYERS, 2, 0.6f);
        this.beginCooldown();
        return true;
    }

    @Override
    protected void onCooldownEnd() {
        if (this.owner.world.isClient && this.owner == MinecraftClient.getInstance().player) {
            RequiemClient.INSTANCE.getRequiemFxRenderer().onPossessionAck();
        } else if (this.target != null) {
            this.getPossessor().startPossessing(this.target);
        }
        this.target = null;
    }

    @Override
    public Identifier getIconTexture() {
        return POSSESSION_ICON;
    }
}
