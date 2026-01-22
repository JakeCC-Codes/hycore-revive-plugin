package com.jakeccz.hyrm.interaction;

import com.google.crypto.tink.subtle.Random;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.jakeccz.hyrm.HycoreReviveMode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static com.jakeccz.hyrm.util.SpectatorUtil.spectatorPlayers;

public class ReviveManager {
    enum StructBlock {
        Wall(),
        Gem(),
        Candle()
    }
    private static final Map<Vector3i, StructBlock> revivePattern = Map.ofEntries(
            Map.entry(new Vector3i(0, -1, 0), StructBlock.Wall),
            Map.entry(new Vector3i(0, -2, 0), StructBlock.Gem),
            Map.entry(new Vector3i(1, -1, 1), StructBlock.Candle),
            Map.entry(new Vector3i(-1, -1, 1), StructBlock.Candle),
            Map.entry(new Vector3i(-1, -1, -1), StructBlock.Candle),
            Map.entry(new Vector3i(1, -1, -1), StructBlock.Candle)
    );
    private static final Vector3i[] torchPattern = {
            new Vector3i(1, -1, 1),
            new Vector3i(-1, -1, 1),
            new Vector3i(-1, -1, -1),
            new Vector3i(1, -1, -1)
    };

    synchronized public static boolean tryRevivePlayer(@NotNull World world, @NotNull PlaceBlockEvent event) {
        Vector3i targetBlockPos = event.getTargetBlock().clone();
        boolean blockSearchResult = testForStruct(targetBlockPos, world);
        if (!blockSearchResult) {
            return false;
        }
        UUID[] specList = spectatorPlayers.toArray(new UUID[0]);
        PlayerRef specTarget = specList.length<1 ? null : Universe.get().getPlayer(specList[(int)(Math.random() * specList.length)]);
        boolean specNotFound = specList.length<1 || specTarget == null;
        (new Thread(() -> {
            setCandleStates(targetBlockPos, world, "Off", 0L, false);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
            setCandleStates(targetBlockPos, world, "On", 750L, specNotFound);
        })).start();

        return true;
    }

    synchronized private static void setCandleStates(Vector3i startPos, @NotNull World world, String newState, long milliSecInterval, boolean cancel) {
        (new Thread(() -> {
            String oldState = Objects.equals(newState, "On") ? "Off" : Objects.equals(newState, "Off") ? "On" : null;
            for (int i=0; i<torchPattern.length; i++) {
                if (cancel && i == torchPattern.length-1) {break;}
                Vector3i pos = startPos.clone().add(torchPattern[i]);
                BlockType bType = world.getBlockType(pos);
                if (bType != null) {
                    world.setBlockInteractionState(pos, bType, newState);
                }
                try {
                    Thread.sleep(milliSecInterval);
                } catch (InterruptedException e) {
                }
            }
            if (cancel) {
                try {
                    Thread.sleep(milliSecInterval + milliSecInterval);
                } catch (InterruptedException e) {
                }
                long SMALLmilliSecInterval = (long) (milliSecInterval * 0.4);
                for (int i=torchPattern.length-2; i>-1; i--) {
                    try {
                        Thread.sleep(SMALLmilliSecInterval);
                    } catch (InterruptedException e) {
                    }
                    Vector3i pos = startPos.clone().add(torchPattern[i]);
                    BlockType bType = world.getBlockType(pos);
                    if (bType != null) {
                        world.setBlockInteractionState(pos, bType, oldState);
                    }
                }
            }
        })).start();
    }

    private static boolean testForStruct(Vector3i startPos, @NotNull World world) {
        AtomicBoolean result = new AtomicBoolean(true);
        revivePattern.forEach((offset, t) -> {
            BlockType block = world.getBlockType(startPos.clone().add(offset));
            if (block == null) {
                result.set(false);
            } else {
                String blockId = block.getId();
                String blockCategory = "";
                if (block.getItem() != null) { blockCategory = block.getItem().getCategories()[0]; } else {
                }
                switch(revivePattern.get(offset)) {
                    case Wall -> {
                        if (!blockId.contains("Wall") || !Objects.equals(blockCategory, "Blocks.Structural")) {
                            result.set(false);
                        }
                    }
                    case Gem -> {
                        if (!blockId.contains("Crystal") || !Objects.equals(blockCategory, "Blocks.Rocks")) {
                            result.set(false);
                        }
                    }
                    case Candle -> {
                        if (!Objects.equals(blockCategory, "Furniture.Lighting") || block.getBlockForState("Off") == null) {
                            result.set(false);
                        }
                    }
                }
            }

        });
        return result.get();
    }
}
