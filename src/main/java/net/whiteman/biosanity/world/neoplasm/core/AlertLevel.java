package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AlertLevel implements StringRepresentable {
    CALM("calm", 0),
    WATCHING("watching", 500),
    STRESSED("stressed", 2000),
    CRITICAL("critical", 5000);

    private final String name;
    private final int alertPoints;

    public static final AlertLevel[] ALERT_LEVELS = AlertLevel.values();

    AlertLevel(String name, int alertPoints) {
        this.name = name;
        this.alertPoints = alertPoints;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static AlertLevel fromPoints(int points) {
        for (int i = ALERT_LEVELS.length - 1; i >= 0; i--) {
            if (points >= ALERT_LEVELS[i].alertPoints) {
                return ALERT_LEVELS[i];
            }
        }
        return CALM;
    }
}
