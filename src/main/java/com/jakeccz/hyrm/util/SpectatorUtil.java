package com.jakeccz.hyrm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;
import com.jakeccz.hyrm.HycoreReviveMode;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SpectatorUtil extends BlockingDiskFile {
    private List<UUID> spectatorPlayers = new ArrayList<>();

    public SpectatorUtil(Path path) {
        super(path);
    }

    public static boolean setGameModeSpectator(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        UUIDComponent uuidComponent = (UUIDComponent) componentAccessor.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return false;
        }
        UUID playerUUID = uuidComponent.getUuid();
        return setGameModeSpectator(ref, playerUUID, componentAccessor);
    }
    public static boolean setGameModeSpectator(@Nonnull Ref<EntityStore> ref, @Nonnull UUID playerUUID, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        Player.setGameMode(ref, GameMode.Creative, componentAccessor);
        hideForAll(ref, playerUUID, componentAccessor.getComponent(ref, PlayerRef.getComponentType()));
        try {
            Player player = componentAccessor.getComponent(ref, Player.getComponentType());
            PlayerRef playerRef = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
            if (player != null && playerRef != null) {
                World world = player.getWorld();
                if (world != null) {
                    CompletableFuture.runAsync(() -> {
                        HudManager hudManager = player.getHudManager();
                        hudManager.hideHudComponents(playerRef, HudComponent.Health, HudComponent.Stamina, HudComponent.InputBindings, HudComponent.Compass, HudComponent.Notifications, HudComponent.ObjectivePanel);
                        hudManager.setCustomHud(playerRef, new SpectatorOverlay(playerRef));
                    }, world);
                    MovementManager movementManager = (MovementManager)componentAccessor.getComponent(ref, MovementManager.getComponentType());
                    if (movementManager != null && movementManager.getDefaultSettings() != null) {
                        movementManager.getSettings().canFly = true;
                        movementManager.getSettings().horizontalFlySpeed = 15.48F;
                        movementManager.getSettings().baseSpeed = 6.875F;
                        movementManager.getSettings().forwardSprintSpeedMultiplier = 2.475F;
                        movementManager.getSettings().forwardRunSpeedMultiplier = 1.5F;
                        movementManager.update(playerRef.getPacketHandler());
                    }
                }
            }
        } catch (Exception e) {
            HycoreReviveMode.getInstance().getLogger().at(Level.INFO).log(e.getMessage());
        }
        return HycoreReviveMode.getInstance().spectatorPlayers.addSpectator(playerUUID);
    }

    public static void hideForAll(Ref<EntityStore> ref, UUID playerUUID, PlayerRef playerRef) {
        Universe.get().getWorlds().forEach((name, w) -> w.execute(() -> {
            for(PlayerRef targetPlayerRef : w.getPlayerRefs()) {
                Ref<EntityStore> targetRef = targetPlayerRef.getReference();
                if (targetRef != null && targetRef.isValid() && !targetRef.equals(ref)) {
                    Store<EntityStore> targetStore = targetRef.getStore();
                    Player targetPlayerComponent = (Player)targetStore.getComponent(targetRef, Player.getComponentType());
                    if (targetPlayerComponent != null) {
                        targetPlayerRef.getHiddenPlayersManager().hidePlayer(playerUUID);
                    }
                }
            }
            //playerRef.sendMessage(Message.translation("server.commands.hide.hiddenFromAll").param("username", playerRef.getUsername()));
            playerRef.sendMessage(Message.translation("hycorerevive.commands.spectator.hidden"));
        }));
    }
    public static void showForAll(Ref<EntityStore> ref, UUID playerUUID) {
        Universe.get().getWorlds().forEach((name, w) -> w.execute(() -> {
            for(PlayerRef targetPlayerRef : w.getPlayerRefs()) {
                Ref<EntityStore> targetRef = targetPlayerRef.getReference();
                if (targetRef != null && targetRef.isValid() && !targetRef.equals(ref)) {
                    Store<EntityStore> targetStore = targetRef.getStore();
                    Player targetPlayerComponent = (Player)targetStore.getComponent(targetRef, Player.getComponentType());
                    if (targetPlayerComponent != null) {
                        targetPlayerRef.getHiddenPlayersManager().showPlayer(playerUUID);
                    }
                }
            }

        }));
    }

    public boolean addSpectator(UUID uuid) {
        this.fileLock.writeLock().lock();
        spectatorPlayers.removeIf((u1) -> u1.equals(uuid));
        boolean result = spectatorPlayers.add(uuid);
        this.fileLock.writeLock().unlock();
        this.syncSave();
        return result;
    }
    public boolean removeSpectator(UUID uuid) {
        this.fileLock.writeLock().lock();
        boolean result = spectatorPlayers.remove(uuid);
        this.fileLock.writeLock().unlock();
        this.syncSave();
        return result;
    }
    public UUID getSpectator(UUID uuid) {
        return (UUID)this.spectatorPlayers.stream().filter((playerUUID) -> playerUUID == uuid).findFirst().orElse((UUID)null);
    }
    public boolean contains(UUID uuid) {
        return this.spectatorPlayers.contains(uuid);
    }
    public List<UUID> getList() {
        return this.spectatorPlayers;
    }
    public List<UUID> getListOnline() {
        return this.spectatorPlayers.stream().filter((uuid) -> {return Universe.get().getPlayer(uuid) != null;}).toList();
    }

    @Override
    protected void read(BufferedReader bufferedReader) throws IOException {
        JsonParser.parseReader(bufferedReader).getAsJsonArray().forEach((entry) -> {
            JsonObject object = entry.getAsJsonObject();

            try {
                spectatorPlayers.add(UUID.fromString(object.get("uuid").getAsString()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse spectator uuid!", e);
            }
        });
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonArray array = new JsonArray();
        spectatorPlayers.forEach((uuid) -> {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", uuid.toString());
            array.add(object);
        });
        bufferedWriter.write(array.toString());
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            jsonWriter.beginArray().endArray();
        }
    }
}
