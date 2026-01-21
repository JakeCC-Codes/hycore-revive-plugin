package com.jakeccz.hyrm.util;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public class SpectatorOverlay extends CustomUIHud {
    public SpectatorOverlay(@NotNull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/SpectatorModeOverlay.ui");
    }
}
