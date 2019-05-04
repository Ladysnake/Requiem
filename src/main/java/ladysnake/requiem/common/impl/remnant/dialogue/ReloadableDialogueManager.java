package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueManager;
import ladysnake.requiem.api.v1.event.minecraft.SyncServerResourcesCallback;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.util.IdentifierAdapter;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReloadableDialogueManager implements SimpleResourceReloadListener<Map<Identifier, DialogueStateMachine>>, DialogueManager, SyncServerResourcesCallback {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(Identifier.class, new IdentifierAdapter()).create();
    public static final int PREFIX_LENGTH = "requiem_dialogues/".length();
    public static final int SUFFIX_LENGTH = ".json".length();

    private final Map<Identifier, DialogueStateMachine> dialogues = new HashMap<>();

    @Override
    public void onServerSync(ServerPlayerEntity player) {
        RequiemNetworking.sendTo(player, RequiemNetworking.createDialogueSyncMessage(this));
    }

    public void applyDialogues(Map<Identifier, DialogueStateMachine> dialogues) {
        this.dialogues.clear();
        this.dialogues.putAll(dialogues);
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(this.dialogues.size());
        for (Map.Entry<Identifier, DialogueStateMachine> entry : this.dialogues.entrySet()) {
            buf.writeString(entry.getKey().toString());
            entry.getValue().writeToPacket(buf);
        }
    }

    @Override
    public CutsceneDialogue getDialogue(Identifier id) {
        if (!this.dialogues.containsKey(id)) {
            throw new IllegalArgumentException("Unknown dialogue " + id);
        }
        return this.dialogues.get(id);
    }

    @Override
    public Identifier getFabricId() {
        return Requiem.id("dialogue_manager");
    }

    @Override
    public CompletableFuture<Map<Identifier, DialogueStateMachine>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, DialogueStateMachine> dialogues = new HashMap<>();
            for (Identifier dialogueLocation : manager.findResources("requiem_dialogues", (res) -> res.endsWith(".json"))) {
                String path = dialogueLocation.getPath();
                Identifier dialogueId = new Identifier(dialogueLocation.getNamespace(), path.substring(PREFIX_LENGTH, path.length() - SUFFIX_LENGTH));
                try (Resource res = manager.getResource(dialogueLocation); Reader in = new InputStreamReader(res.getInputStream())) {
                    dialogues.put(dialogueId, GSON.fromJson(in, DialogueStateMachine.class));
                } catch (IOException e) {
                    Requiem.LOGGER.error("Could not read dialogue {}", dialogueLocation);
                }
            }
            return dialogues;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, DialogueStateMachine> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> this.applyDialogues(data), executor);
    }

    public static Map<Identifier, DialogueStateMachine> fromPacket(PacketByteBuf buf) {
        Map<Identifier, DialogueStateMachine> dialogues = new HashMap<>();
        int nbDialogues = buf.readVarInt();
        for (int i = 0; i < nbDialogues; i++) {
            dialogues.put(Identifier.create(buf.readString()), new DialogueStateMachine().readFromPacket(buf));
        }
        return dialogues;
    }

}
