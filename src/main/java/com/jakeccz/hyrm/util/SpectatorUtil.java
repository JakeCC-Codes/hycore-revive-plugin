package com.jakeccz.hyrm.util;

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

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SpectatorUtil {
    public static final Set<UUID> spectatorPlayers = ConcurrentHashMap.newKeySet();

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
        }
        return spectatorPlayers.add(playerUUID);
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
}
