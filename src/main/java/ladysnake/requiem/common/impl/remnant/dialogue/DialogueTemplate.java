package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.common.util.MoreCodecs;

import java.util.Map;

public record DialogueTemplate(String start, Map<String, DialogueState> states) {
    public static final Codec<DialogueTemplate> CODEC = codec(MoreCodecs.DYNAMIC_JSON);
    public static final Codec<DialogueTemplate> NETWORK_CODEC = codec(MoreCodecs.STRING_JSON);

    private static Codec<DialogueTemplate> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("start_at").forGetter(DialogueTemplate::start),
            Codec.unboundedMap(Codec.STRING, DialogueState.codec(jsonCodec)).fieldOf("states").forGetter(DialogueTemplate::states)
        ).apply(instance, DialogueTemplate::new));
    }
}
