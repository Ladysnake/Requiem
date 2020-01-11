package ladysnake.requiem.mixin.client.render.model;

import ladysnake.requiem.Requiem;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelItemOverride;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Unique
    private static final Identifier ENCHANTED_BOOK_ID = new Identifier("item/enchanted_book");

    @ModifyVariable(method = "loadModelFromJson", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"))
    private JsonUnbakedModel addEnchantedBookOverride(JsonUnbakedModel model, Identifier id) {
        if (ENCHANTED_BOOK_ID.equals(id)) {
            Map<Identifier, Float> minPropertyValues = new HashMap<>();
            minPropertyValues.put(Requiem.id("humanity"), 1F);
            model.getOverrides().add(new ModelItemOverride(Requiem.id("item/humanity_enchanted_book"), minPropertyValues));
        }
        return model;
    }
}
