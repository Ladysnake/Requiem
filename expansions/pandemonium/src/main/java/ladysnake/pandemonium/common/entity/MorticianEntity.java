package ladysnake.pandemonium.common.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MorticianEntity extends MerchantEntity {
    public MorticianEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {

    }

    @Override
    protected void fillRecipes() {

    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean canRefreshTrades() {
        return super.canRefreshTrades();
    }

    @Override
    public void sendOffers(PlayerEntity playerEntity, Text text, int i) {
        super.sendOffers(playerEntity, text, i);
    }
}
