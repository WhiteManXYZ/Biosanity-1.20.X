package net.whiteman.biosanity.block.custom.neoplasm;

public interface INeoplasmNode {
    default boolean isCore() {
        return false;
    }
}
