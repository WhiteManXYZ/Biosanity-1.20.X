package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CoreLevel implements StringRepresentable {
    T1("tier_1", 0),
    T2("tier_2", 120),
    T3("tier_3", 1000),
    T4("tier_4", 5000),
    T5("tier_5", 30_000);

    private final String name;
    private final int xp;

    CoreLevel(String name, int xp) {
        this.name = name;
        this.xp = xp;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public int getNeededXp() { return xp; }

    public static int getStartingXp() { return T1.xp; }
}