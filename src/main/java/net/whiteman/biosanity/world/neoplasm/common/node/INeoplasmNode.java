package net.whiteman.biosanity.world.neoplasm.common.node;

public interface INeoplasmNode {
    default boolean isCore() {
        return false;
    }
}
