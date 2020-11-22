package ladysnake.requiem.api.v1.entity;

import com.google.common.collect.ImmutableList;

public enum InventoryPart {
    MAIN, HANDS, ARMOR, CRAFTING;

    public static final ImmutableList<InventoryPart> VALUES = ImmutableList.copyOf(values());
}
