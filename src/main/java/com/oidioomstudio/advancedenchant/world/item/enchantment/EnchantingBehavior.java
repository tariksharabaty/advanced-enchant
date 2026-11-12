package com.oidioomstudio.advancedenchant.world.item.enchantment;

import com.oidioomstudio.advancedenchant.world.level.block.InfuserType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public interface EnchantingBehavior {
    static EnchantingBehavior get() {
        return VanillaEnchantingBehavior.enchantingBehavior;
    }

    static void set(EnchantingBehavior enchantingBehavior) {
        VanillaEnchantingBehavior.enchantingBehavior = enchantingBehavior;
    }

    Collection<String> getScalingNamespaces();

    int getEnchantmentPowerLimit(InfuserType type);

    float getEnchantmentPower(BlockState state, Level level, BlockPos pos);

    float getEnchantmentPowerLimitScale(BlockState state, Level level, BlockPos pos);

    float getMaximumCostMultiplier();

    int getMaxLevel(Holder<Enchantment> enchantment);

    int getMinCost(Holder<Enchantment> enchantment, int level);

    int getMaxCost(Holder<Enchantment> enchantment, int level);
}
