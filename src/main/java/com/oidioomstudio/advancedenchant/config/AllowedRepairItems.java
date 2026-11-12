package com.oidioomstudio.advancedenchant.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public enum AllowedRepairItems {
    EVERYTHING,
    TOOLS_AND_ARMOR,
    NOTHING;

    public boolean canRepair(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamaged()) {
            return false;
        }
        if (this == TOOLS_AND_ARMOR) {
            return itemStack.has(DataComponents.MAX_DAMAGE);
        }
        return this == EVERYTHING;
    }

    public boolean isActive() {
        return this != NOTHING;
    }
}
