package ladysnake.requiem.common.advancement.criterion;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

public abstract class RemnantTypePredicate {
    public static final RemnantTypePredicate ANY = new RemnantTypePredicate() {
        public boolean matches(RemnantType type) {
            return true;
        }

        public JsonElement serialize() {
            return JsonNull.INSTANCE;
        }
    };
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    public abstract boolean matches(RemnantType var1);

    public abstract JsonElement serialize();

    public static RemnantTypePredicate deserialize(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            Identifier id = new Identifier(JsonHelper.asString(json, "type"));
            RemnantType type = (RemnantType) Registry.ENTITY_TYPE.getOrEmpty(id).orElseThrow(() -> new JsonSyntaxException("Unknown remnant type '" + id + "', valid types are: " + COMMA_JOINER.join(RequiemRegistries.REMNANT_STATES.getIds())));
            return new Single(type);
        } else {
            return ANY;
        }
    }
    static class Single extends RemnantTypePredicate {
        private final RemnantType type;

        public Single(RemnantType type) {
            this.type = type;
        }

        public boolean matches(RemnantType type) {
            return this.type == type;
        }

        public JsonElement serialize() {
            return new JsonPrimitive(RequiemRegistries.REMNANT_STATES.getId(this.type).toString());
        }
    }

}
