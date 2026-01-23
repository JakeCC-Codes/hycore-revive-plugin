package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.HycoreReviveMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DropPreventSystem extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {
    public DropPreventSystem() {
        super(DropItemEvent.PlayerRequest.class);
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> chunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull DropItemEvent.PlayerRequest event) {
        Ref<EntityStore> ref = chunk.getReferenceTo(i);
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent != null) {
            if (HycoreReviveMode.getInstance().spectatorPlayers.contains(uuidComponent.getUuid())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
