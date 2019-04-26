package ladysnake.requiem.mixin.block.entity;

import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.item.WrittenOpusItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin extends BlockEntity {
    @Shadow private ItemStack book;

    public LecternBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "hasBook", at = @At("RETURN"), cancellable = true)
    private void hasBook(CallbackInfoReturnable<Boolean> cir) {
        Item bookItem = this.book.getItem();
        if (bookItem instanceof OpusDemoniumItem || bookItem instanceof WrittenOpusItem) {
            cir.setReturnValue(true);
        }
    }
}
