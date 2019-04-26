package ladysnake.requiem.common.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringTag;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleRemnantType implements RemnantType {
    protected final Function<PlayerEntity, RemnantState> factory;
    protected final String conversionSentence;
    protected final Supplier<Item> conversionBook;

    public SimpleRemnantType(Function<PlayerEntity, RemnantState> factory, String conversionSentence, Supplier<Item> conversionBook) {
        this.factory = factory;
        this.conversionSentence = conversionSentence;
        this.conversionBook = conversionBook;
    }

    @Override
    public RemnantState create(PlayerEntity player) {
        return this.factory.apply(player);
    }

    @Nullable
    @Override
    public String getConversionBookSentence() {
        return conversionSentence;
    }

    @Override
    public ItemStack getConversionBook(@Nullable PlayerEntity player) {
        ItemStack ret = new ItemStack(this.conversionBook.get());
        if (player != null) {
            ret.setChildTag("author", new StringTag(player.getGameProfile().getName()));
        }
        return ret;
    }
}
