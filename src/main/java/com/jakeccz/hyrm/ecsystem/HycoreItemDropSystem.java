package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.HycoreReviveMode;

import javax.annotation.Nonnull;
import java.util.List;

public class HycoreItemDropSystem extends DeathSystems.DropPlayerDeathItems {

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
        component.setItemsLossMode(DeathConfig.ItemsLossMode.ALL); // Drop ALL Items
        component.setItemsDurabilityLossPercentage(HycoreReviveMode.ITEMDURABILITYLOSSPERCENTAGE);
        component.setItemsAmountLossPercentage(HycoreReviveMode.ITEMAMOUNTLOSSPERCENTAGE);

        assert playerComponent != null;

        if (playerComponent.getGameMode() != GameMode.Creative) {
            component.setDisplayDataOnDeathScreen(true);
            CombinedItemContainer combinedItemContainer = playerComponent.getInventory().getCombinedEverything();
            if (component.getItemsDurabilityLossPercentage() > (double)0.0F) {
                double durabilityLossRatio = component.getItemsDurabilityLossPercentage() / (double)100.0F;
                boolean hasArmorBroken = false;

                for(short i = 0; i < combinedItemContainer.getCapacity(); ++i) {
                    ItemStack itemStack = combinedItemContainer.getItemStack(i);
                    if (!ItemStack.isEmpty(itemStack) && !itemStack.isBroken()) {
                        double durabilityLoss = itemStack.getMaxDurability() * durabilityLossRatio;
                        ItemStack updatedItemStack = itemStack.withIncreasedDurability(-durabilityLoss);
                        ItemStackSlotTransaction transaction = combinedItemContainer.replaceItemStackInSlot(i, itemStack, updatedItemStack);
                        if (transaction.getSlotAfter().isBroken() && itemStack.getItem().getArmor() != null) {
                            hasArmorBroken = true;
                        }
                    }
                }

                if (hasArmorBroken) {
                    playerComponent.getStatModifiersManager().setRecalculate(true);
                }
            }

            List<ItemStack> itemsToDrop = playerComponent.getInventory().dropAllItemStacks();

            if (itemsToDrop != null && !itemsToDrop.isEmpty()) {
                TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
                if (transformComponent != null) {
                    Vector3d position = transformComponent.getPosition();
                    HeadRotation headRotationComponent = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());
                    if (headRotationComponent != null) {
                        Vector3f headRotation = headRotationComponent.getRotation();
                        Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, itemsToDrop, position.clone().add((double)0.0F, (double)1.0F, (double)0.0F), headRotation);
                        commandBuffer.addEntities(drops, AddReason.SPAWN);
                    }
                }
                component.setItemsLostOnDeath(itemsToDrop);
            }

        }
    }
}
