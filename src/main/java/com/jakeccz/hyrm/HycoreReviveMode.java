package com.jakeccz.hyrm;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.jakeccz.hyrm.command.SpectatorGameModeCommand;
import com.jakeccz.hyrm.ecsystem.*;

public class HycoreReviveMode extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static HycoreReviveMode INSTANCE;

    public HycoreReviveMode(JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    public static HycoreReviveMode getInstance() {return INSTANCE;}

    @Override
    protected void setup() {
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
}
