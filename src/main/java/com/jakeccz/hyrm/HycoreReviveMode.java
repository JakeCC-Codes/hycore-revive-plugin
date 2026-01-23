package com.jakeccz.hyrm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.jakeccz.hyrm.command.SpectatorGameModeCommand;
import com.jakeccz.hyrm.ecsystem.*;
import com.jakeccz.hyrm.util.SpectatorUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HycoreReviveMode extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static HycoreReviveMode INSTANCE;
    public SpectatorUtil spectatorPlayers;

    public HycoreReviveMode(JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        LOGGER.atInfo().log("%s[v%s] successfully installed!", this.getName(), this.getManifest().getVersion().toString());
        this.spectatorPlayers = new SpectatorUtil(Path.of("JakeCCz/spectatorPlayers.json"));
        // TODO: Config that makes you stay in spectator mode when leaving and rejoining
    }

    public static HycoreReviveMode getInstance() {return INSTANCE;}

    @Override
    protected void setup() {
        File folder = new File("JakeCCz");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        this.spectatorPlayers.syncLoad();
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
            Ref<EntityStore> ref = event.getPlayerRef();
            Store<EntityStore> store = ref.getStore();
            Player player = event.getPlayer();
            assert player.getWorld() != null;
            CompletableFuture.runAsync(() -> {
                UUIDComponent u = (UUIDComponent)store.getComponent(ref, UUIDComponent.getComponentType()); UUID playerUUID = u == null ? null : u.getUuid();
                PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());
                if (this.spectatorPlayers.contains(playerUUID)) {
                    SpectatorUtil.setGameModeSpectator(ref, playerUUID, store);
                } else {
                    if (playerRef != null && playerRef.isValid()) {
                        this.spectatorPlayers.getList().forEach((uuid -> {
                            playerRef.getHiddenPlayersManager().hidePlayer(uuid);
                        }));
                    }
                }
            }, player.getWorld());

        });
        this.getCommandRegistry().registerCommand(new SpectatorGameModeCommand());
        (new Thread(() -> {
            while(DeathComponent.getComponentType() == null) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException var2) {
                }
            }

            this.getEntityStoreRegistry().registerSystem(new HycoreDeathSystem());
            this.getEntityStoreRegistry().registerSystem(new AttackPreventSystem());
            this.getEntityStoreRegistry().registerSystem(new BreakPreventSystem());
            this.getEntityStoreRegistry().registerSystem(new PlacePreventSystem());
            this.getEntityStoreRegistry().registerSystem(new GamemodeChangeSystem());
            this.getEntityStoreRegistry().registerSystem(new DropPreventSystem());
            this.getEntityStoreRegistry().registerSystem(new PickUpPreventSystem());
        })).start();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        this.spectatorPlayers.syncSave();
    }
}
