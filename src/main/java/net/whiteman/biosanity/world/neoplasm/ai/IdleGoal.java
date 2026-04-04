package net.whiteman.biosanity.world.neoplasm.ai;

import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBlockEntity;

public class IdleGoal implements ICoreGoal {
    private final NeoplasmCoreBlockEntity core;
    private final int priority;
    private int cooldown = 0;

    public IdleGoal(NeoplasmCoreBlockEntity core, int priority) {
        this.core = core;
        this.priority = priority;
    }

    @Override
    public GoalCategory getCategory() { return GoalCategory.IDLE; }

    @Override
    public int getPriority() { return priority; }

    @Override
    public boolean canUse() { return true; }

    @Override
    public void start() {}

    @Override
    public void tick(Hivemind hive) {}

    @Override
    public void stop() {}
}
