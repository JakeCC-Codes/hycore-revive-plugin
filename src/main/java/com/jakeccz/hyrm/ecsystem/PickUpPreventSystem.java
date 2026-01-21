package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PickupItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

import static com.jakeccz.hyrm.util.SpectatorUtil.spectatorPlayers;

public class PickUpPreventSystem extends EntityTickingSystem<EntityStore> {
    private static final float EYE_HEIGHT_SCALE = 5.0F;
    @Nonnull
    private final ComponentType<EntityStore, PickupItemComponent> pickupItemComponentType;
    @Nonnull
    private final ComponentType<EntityStore, TransformComponent> transformComponentType;
    @Nonnull
    private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
    @Nonnull
    private final Query<EntityStore> query;

    public PickUpPreventSystem() {
        this.pickupItemComponentType = PickupItemComponent.getComponentType();
        this.transformComponentType = TransformComponent.getComponentType();
        this.playerSpatialComponent = EntityModule.get().getPlayerSpatialResourceType();
        this.query = Query.and(new Query[]{pickupItemComponentType, transformComponentType});
    }

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PickupItemComponent pickupItemComponent = (PickupItemComponent)archetypeChunk.getComponent(index, this.pickupItemComponentType);
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource)store.getResource(this.playerSpatialComponent);
        SpatialStructure<Ref<EntityStore>> spatialStructure = playerSpatialResource.getSpatialStructure();

        assert pickupItemComponent != null;

        TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        assert transformComponent != null;
        Vector3d itemEntityPosition = transformComponent.getPosition();
        Ref<EntityStore> targetRef = (Ref)spatialStructure.closest(itemEntityPosition);
        if (targetRef != null && targetRef.getStore().getComponent(targetRef, UUIDComponent.getComponentType()) instanceof UUIDComponent uuidComponent) {
            if (spectatorPlayers.contains(uuidComponent.getUuid())) {
                commandBuffer.tryRemoveComponent(archetypeChunk.getReferenceTo(index), this.pickupItemComponentType);
                commandBuffer.run((cb) -> {
                    Player player = targetRef.getStore().getComponent(targetRef, Player.getComponentType());
                    assert player != null;
                    player.setInventory(new Inventory());
                });
            }

        }

    }

    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
