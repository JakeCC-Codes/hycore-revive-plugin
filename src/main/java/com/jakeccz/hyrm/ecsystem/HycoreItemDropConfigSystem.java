package com.jakeccz.hyrm.ecsystem;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.HycoreReviveMode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HycoreItemDropConfigSystem extends DeathSystems.PlayerDropItemsConfig {

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathConfig deathConfig = ((EntityStore)store.getExternalData()).getWorld().getDeathConfig();
        component.setItemsLossMode(DeathConfig.ItemsLossMode.ALL); // Drop ALL Items
        component.setItemsDurabilityLossPercentage(HycoreReviveMode.ITEMDURABILITYLOSSPERCENTAGE);
        component.setItemsAmountLossPercentage(HycoreReviveMode.ITEMAMOUNTLOSSPERCENTAGE);

        Player playerComp = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerComp != null) {
            CombinedItemContainer combinedItemContainer = playerComp.getInventory().getCombinedEverything();
            List<ItemStack> itemsToDrop = new ObjectArrayList();
            for(short i = 0; i < combinedItemContainer.getCapacity(); ++i) {
                ItemStack itemStack = combinedItemContainer.getItemStack(i);
                if (!ItemStack.isEmpty(itemStack)) {
                    itemsToDrop.add(itemStack.withQuantity(itemStack.getQuantity()));
                }
            }
            component.setItemsLostOnDeath(itemsToDrop);
        }
        component.setDisplayDataOnDeathScreen(playerComp == null || playerComp.getGameMode() != GameMode.Creative);
    }

    @Override
    public @Nonnull Set<Dependency<EntityStore>> getDependencies() {
        Set<Dependency<EntityStore>> depens = new LinkedHashSet();

        try {
            Class<?> checkClazz = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems$DropPlayerDeathItems");
            if (checkClazz != null) {
                depens.add(new SystemDependency(Order.AFTER, checkClazz));
            }
        } catch (ClassNotFoundException e) {
        }
        return depens;
    }

}
