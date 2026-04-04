package net.whiteman.biosanity.world.neoplasm.ai;

import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;

public interface ICoreGoal {
    GoalCategory getCategory();

    int getPriority();

    default int getCost() {
        return 0;
    }

    boolean canUse();

    default boolean canContinueToUse() {
        return canUse();
    }

    /**
     * If the goal is "unique" (e.g., building special block),
     * then we return true only if no one has taken it.
     * If the goal is "scalable" (e.g., energy production), everyone can tick it.
     */
    default boolean isStackable() {
        return false;
    }

    void start();
    void tick(Hivemind hive);
    void stop();
}