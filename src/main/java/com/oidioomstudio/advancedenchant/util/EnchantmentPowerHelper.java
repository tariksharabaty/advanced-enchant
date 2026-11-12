package com.oidioomstudio.advancedenchant.util;

import com.oidioomstudio.advancedenchant.world.item.enchantment.EnchantingBehavior;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.math.Fraction;

import java.util.Collection;

public class EnchantmentPowerHelper {
    public static Object2IntMap<Holder<Enchantment>> getAvailableEnchantmentLevels(int enchantmentPower, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        Object2IntOpenHashMap<Holder<Enchantment>> maximumEnchantmentLevels = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            int enchantmentLevel = getAvailableEnchantmentLevel(enchantment, enchantmentPower, itemEnchantments, powerLimit, enchantmentValue);
            maximumEnchantmentLevels.put(enchantment, enchantmentLevel);
        }
        return Object2IntMaps.unmodifiable(maximumEnchantmentLevels);
    }

    public static int getAvailableEnchantmentLevel(Holder<Enchantment> enchantment, int enchantmentPower, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        for (int enchantmentLevel = EnchantingBehavior.get().getMaxLevel(enchantment); enchantmentLevel > 0; --enchantmentLevel) {
            int powerForLevel = getScaledPowerForLevel(enchantment, enchantmentLevel, itemEnchantments, powerLimit, enchantmentValue);
            if (powerForLevel > enchantmentPower) continue;
            return enchantmentLevel;
        }
        return 0;
    }

    public static Object2IntMap<Holder<Enchantment>> getRequiredEnchantmentPowers(int enchantmentPower, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        Object2IntOpenHashMap<Holder<Enchantment>> requiredEnchantmentPowers = new Object2IntOpenHashMap<>();
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            int enchantmentLevel = getRequiredEnchantmentPower(enchantment, enchantmentPower, itemEnchantments, powerLimit, enchantmentValue);
            requiredEnchantmentPowers.put(enchantment, enchantmentLevel);
        }
        return Object2IntMaps.unmodifiable(requiredEnchantmentPowers);
    }

    public static int getRequiredEnchantmentPower(Holder<Enchantment> enchantment, int enchantmentPower, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        int requiredEnchantmentPower = 0;
        for (int enchantmentLevel = EnchantingBehavior.get().getMaxLevel(enchantment); enchantmentLevel >= 0; --enchantmentLevel) {
            int powerForLevel = getScaledPowerForLevel(enchantment, enchantmentLevel, itemEnchantments, powerLimit, enchantmentValue);
            if (powerForLevel <= enchantmentPower) {
                return requiredEnchantmentPower;
            }
            requiredEnchantmentPower = powerForLevel;
        }
        return requiredEnchantmentPower;
    }

    public static int getScaledPowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel, Collection<Holder<Enchantment>> itemEnchantments, int powerLimit, int enchantmentValue) {
        Fraction relativePowerForLevel = getRelativePowerForLevel(enchantment, enchantmentLevel, itemEnchantments);
        Fraction enchantmentValueBonus = Fraction.getFraction(enchantmentValue, 60 * powerLimit);
        Fraction reducedPowerLimit = Fraction.getFraction(powerLimit, 1).subtract(enchantmentValueBonus);
        if (reducedPowerLimit.compareTo(Fraction.ZERO) < 0) {
            reducedPowerLimit = Fraction.ZERO;
        }
        int scaledPowerForLevel = Math.round(relativePowerForLevel.multiplyBy(reducedPowerLimit).floatValue());
        if (enchantment.is(EnchantmentTags.CURSE)) {
            scaledPowerForLevel *= 3;
        } else if (enchantment.is(EnchantmentTags.TREASURE)) {
            scaledPowerForLevel *= 2;
        }
        return Mth.clamp(scaledPowerForLevel, 0, powerLimit);
    }

    private static Fraction getRelativePowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel, Collection<Holder<Enchantment>> itemEnchantments) {
        int minPower = getMinPower(itemEnchantments);
        int maxPower = getMaxPower(itemEnchantments);
        int powerForLevel = getPowerForLevel(enchantment, enchantmentLevel);
        if (maxPower == minPower) {
            return Fraction.ONE;
        }
        return Fraction.getFraction(powerForLevel - minPower, maxPower - minPower);
    }

    private static int getMinPower(Collection<Holder<Enchantment>> itemEnchantments) {
        if (itemEnchantments.isEmpty()) {
            return 0;
        }
        int sum = 0;
        int min = Integer.MAX_VALUE;
        for (Holder<Enchantment> holder : itemEnchantments) {
            int power = getPowerForLevel(holder, holder.value().getMinLevel());
            sum += power;
            if (power < min) {
                min = power;
            }
        }
        int averageMinStrength = sum / itemEnchantments.size();
        int absoluteMinStrength = (min == Integer.MAX_VALUE) ? 0 : min;
        return (averageMinStrength + absoluteMinStrength) / 2;
    }

    private static int getMaxPower(Collection<Holder<Enchantment>> itemEnchantments) {
        if (itemEnchantments.isEmpty()) {
            return 0;
        }
        int sum = 0;
        int max = Integer.MIN_VALUE;
        for (Holder<Enchantment> enchantment : itemEnchantments) {
            int power = getPowerForLevel(enchantment, EnchantingBehavior.get().getMaxLevel(enchantment));
            sum += power;
            if (power > max) {
                max = power;
            }
        }
        int averageMaxStrength = sum / itemEnchantments.size();
        int absoluteMaxStrength = (max == Integer.MIN_VALUE) ? 0 : max;
        return (averageMaxStrength + absoluteMaxStrength) / 2;
    }

    private static int getPowerForLevel(Holder<Enchantment> enchantment, int enchantmentLevel) {
        int minCost = EnchantingBehavior.get().getMinCost(enchantment, enchantmentLevel);
        int maxCost = EnchantingBehavior.get().getMaxCost(enchantment, enchantmentLevel);
        return minCost + (maxCost - minCost) * enchantmentLevel / (EnchantingBehavior.get().getMaxLevel(enchantment) + 1);
    }
}
