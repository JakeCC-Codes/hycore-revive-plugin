package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.util.SpectatorUtil;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class HycoreDeathSystem extends DeathSystems.OnDeathSystem {

    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent component, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player playerComp = store.getComponent(ref, Player.getComponentType());
        UUIDComponent uuidComp = store.getComponent(ref, UUIDComponent.getComponentType());
        if (playerRef != null && playerComp != null && uuidComp != null) {
            commandBuffer.run((cb) -> this.onDeathReady(ref, store, playerRef, playerComp, uuidComp.getUuid(), commandBuffer));
        }
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        UUIDComponent uuidComp = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComp != null) {
            commandBuffer.run((cb) -> SpectatorUtil.setGameModeSpectator(ref, uuidComp.getUuid(), store));
        }
    }

    private void onDeathReady(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, Player playerComp, UUID playerUUID, CommandBuffer<EntityStore> commandBuffer) {
        BsonDocument metadata = new BsonDocument();
        metadata.append("Username", new BsonString(playerRef.getUsername()));
        metadata.append("UUID", new BsonString(playerUUID.toString()));
        ItemStack playerHead = new ItemStack("Revive_Head").withState("EntityDropped").withMetadata("SkullOwner", metadata);
        TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent != null)
        {
            Vector3d dropPos = transformComponent.getPosition().clone();
            Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(store, playerHead, dropPos, Vector3f.ZERO, 0F, 0F, 0F);
            if (itemEntityHolder != null) {
                ItemComponent itemComponent = (ItemComponent) itemEntityHolder.getComponent(ItemComponent.getComponentType());
                if (itemComponent != null) {
                    itemComponent.setPickupDelay(1.5F);
                }
                commandBuffer.addEntity(itemEntityHolder, AddReason.SPAWN);
            }
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
