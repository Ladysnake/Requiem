package ladysnake.dissolution.common.impl.movement;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.EntityType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class EntityTypeAdapter extends TypeAdapter<EntityType<?>> {
    @Override
    public void write(JsonWriter out, EntityType<?> value) throws IOException {
        out.value(Objects.requireNonNull(EntityType.getId(value)).toString());
    }

    @Nullable
    @Override
    public EntityType<?> read(JsonReader in) throws IOException {
        return EntityType.get(in.nextString()).orElse(null);
    }
}
