package com.jakeccz.hyrm.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.util.SpectatorUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;


public class SpectatorGameModeCommand extends AbstractAsyncCommand {
    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    public SpectatorGameModeCommand() {
        super("gms", "hycorerevive.commands.spectator.desc");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
    }

    @Override
    protected @NotNull CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        PlayerRef playerRef = (PlayerRef)this.playerArg.get(context);
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            return this.runAsync(context, () -> {
                SpectatorUtil.setGameModeSpectator(ref, store);
            }, world);
        } else {
            return CompletableFuture.completedFuture((Void) null);
        }
    }
}