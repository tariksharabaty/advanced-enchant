package com.oidioomstudio.advancedenchant.config;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public enum ModifiableItems {
    UNENCHANTED(ItemStack::isEnchantable),
    ALL(ModifiableItems::isItemEnchantableOrEnchanted),
    FULL_DURABILITY(itemStack -> !itemStack.isDamaged() && isItemEnchantableOrEnchanted(itemStack));

    public final Predicate<ItemStack> predicate;

    ModifiableItems(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    private static boolean isItemEnchantableOrEnchanted(ItemStack itemStack) {
        return EnchantmentHelper.canStoreEnchantments(itemStack) || itemStack.isEnchanted();
    }
}
