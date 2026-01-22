package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.interaction.ReviveManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.jakeccz.hyrm.util.SpectatorUtil.spectatorPlayers;

public class PlacePreventSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    public PlacePreventSystem() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> chunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull PlaceBlockEvent event) {
        Ref<EntityStore> ref = chunk.getReferenceTo(i);
        Player player = (Player)store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent != null) {
                if (spectatorPlayers.contains(uuidComponent.getUuid())) {
                    event.setCancelled(true);
                } else {
                    World world = player.getWorld();
                    if (world != null && (event.getItemInHand().getItemId().equals("Revive_Head") || event.getItemInHand().getItemId().equals("Revive_Head_State_EntityDropped"))) {
                        CompletableFuture.runAsync(() -> ReviveManager.tryRevivePlayer(world, event), world);
                    }
                }

            }
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
