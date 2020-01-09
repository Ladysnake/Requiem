/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueAction;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.common.util.IdentifierAdapter;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class DialogueManager implements SubDataManager<Map<Identifier, DialogueStateMachine>>, DialogueRegistry {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
            .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
            .create();
    public static final int PREFIX_LENGTH = "requiem_dialogues/".length();
    public static final int SUFFIX_LENGTH = ".json".length();

    private final Map<Identifier, DialogueStateMachine> dialogues = new HashMap<>();
    private final Map<Identifier, DialogueAction> actions = new HashMap<>();

    @Override
    public synchronized void apply(Map<Identifier, DialogueStateMachine> dialogues) {
        this.dialogues.clear();
        this.dialogues.putAll(dialogues);
        Requiem.LOGGER.info("[Requiem] Added dialogues {}", dialogues.keySet());
    }

    @Override
    public Map<Identifier, DialogueStateMachine> loadFromPacket(PacketByteBuf buf) {
        Map<Identifier, DialogueStateMachine> dialogues = new HashMap<>();
        int nbDialogues = buf.readVarInt();
        for (int i = 0; i < nbDialogues; i++) {
            dialogues.put(Identifier.tryParse(buf.readString()), new DialogueStateMachine().readFromPacket(buf));
        }
        return dialogues;
    }

    public synchronized void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(this.dialogues.size());
        for (Map.Entry<Identifier, DialogueStateMachine> entry : this.dialogues.entrySet()) {
            buf.writeString(entry.getKey().toString());
            entry.getValue().writeToPacket(buf);
        }
    }

    @Override
    public synchronized CutsceneDialogue getDialogue(Identifier id) {
        if (!this.dialogues.containsKey(id)) {
            throw new IllegalArgumentException("Unknown dialogue " + id);
        }
        return this.dialogues.get(id);
    }

    @Override
    public void registerAction(Identifier actionId, DialogueAction action) {
        this.actions.put(actionId, action);
    }

    @Override
    public DialogueAction getAction(Identifier actionId) {
        if (!this.actions.containsKey(actionId)) {
            Requiem.LOGGER.warn("[Requiem] Unknown dialogue action {}", actionId);
            return DialogueAction.NONE;
        }
        return this.actions.get(actionId);
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
            Requiem.LOGGER.info("[Requiem] Parsed dialogues {}", dialogues.keySet());
            return dialogues;
        }, executor);
    }

    @Override
    public String toString() {
        return "ReloadableDialogueRegistry{" +
                "dialogues=" + dialogues.keySet() +
                ", actions=" + actions.keySet() +
                '}';
    }
}
