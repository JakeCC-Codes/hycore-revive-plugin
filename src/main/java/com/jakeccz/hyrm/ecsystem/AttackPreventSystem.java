package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.HycoreReviveMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttackPreventSystem extends DamageEventSystem {
    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> chunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull Damage damage) {
        try {
            if (damage.getSource() instanceof Damage.EntitySource source) {
                if (source.getRef() instanceof Ref<EntityStore> ref && ref.isValid()) {
                    if (ref.getStore().getComponent(ref, UUIDComponent.getComponentType()) instanceof UUIDComponent uuidComp) {
                        if (HycoreReviveMode.getInstance().spectatorPlayers.contains(uuidComp.getUuid())) {
                            damage.setAmount(0);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(UUIDComponent.getComponentType());
    }

    @Override
    @Nullable
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }
}
