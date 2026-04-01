package net.whiteman.biosanity.world.neoplasm.core;

import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;

public class CoreConfig {
    /// WIP
    public static final int MAX_XP = 1000;
    public static final int MAX_ALERT_POINTS = 10000;
    public static final int RESOURCE_LEVEL_MULTIPLIER = 10;
    public static final int[] XP_VALUES = new int[MAX_RESOURCE_LEVEL + 1];

    static {
        for (int i = 0; i <= MAX_RESOURCE_LEVEL; i++) {
            if (i == 0) {
                XP_VALUES[i] = 0;
            } else {
                double power = i - 1;
                XP_VALUES[i] = (int) (RESOURCE_LEVEL_MULTIPLIER * Math.pow(3, power));
            }
        }
    }

    public static int getXPFromLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_RESOURCE_LEVEL));
        return XP_VALUES[clampedLevel];
    }
}