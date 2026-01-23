package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.HycoreReviveMode;
import com.jakeccz.hyrm.util.EmptyOverlay;
import com.jakeccz.hyrm.util.SpectatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GamemodeChangeSystem extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {
    public GamemodeChangeSystem() {
        super(ChangeGameModeEvent.class);
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> chunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull ChangeGameModeEvent event) {
        Ref<EntityStore> ref = chunk.getReferenceTo(i);
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent != null) {
            UUID playerUUID = uuidComponent.getUuid();
            SpectatorUtil spectatorPlayers = HycoreReviveMode.getInstance().spectatorPlayers;
            if (spectatorPlayers.contains(playerUUID)) {
                spectatorPlayers.removeSpectator(playerUUID);
                SpectatorUtil.showForAll(ref, playerUUID);

                Player player = (Player)commandBuffer.getComponent(ref, Player.getComponentType());
                assert player != null;
                PlayerRef playerRef = (PlayerRef) commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                assert playerRef != null && playerRef.isValid();
                World world = player.getWorld();
                player.getWorldMapTracker().tick(0);
                assert world != null;
                CompletableFuture.runAsync(() -> {
                    HudManager hudManager = player.getHudManager();
                    hudManager.showHudComponents(playerRef, HudComponent.Health, HudComponent.Stamina, HudComponent.InputBindings, HudComponent.Compass, HudComponent.Notifications, HudComponent.ObjectivePanel);
                    hudManager.setCustomHud(playerRef, new EmptyOverlay(playerRef));
                }, world);
                MovementManager movementManager = (MovementManager)store.getComponent(ref, MovementManager.getComponentType());
                if (movementManager != null && movementManager.getDefaultSettings() != null) {
                    movementManager.getSettings().canFly = movementManager.getDefaultSettings().canFly;
                    movementManager.getSettings().horizontalFlySpeed = movementManager.getDefaultSettings().horizontalFlySpeed;
                    movementManager.getSettings().baseSpeed = movementManager.getDefaultSettings().baseSpeed;
                    movementManager.getSettings().forwardSprintSpeedMultiplier = movementManager.getDefaultSettings().forwardSprintSpeedMultiplier;
                    movementManager.getSettings().forwardRunSpeedMultiplier = movementManager.getDefaultSettings().forwardRunSpeedMultiplier;
                    movementManager.update(playerRef.getPacketHandler());
                }
            }
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
