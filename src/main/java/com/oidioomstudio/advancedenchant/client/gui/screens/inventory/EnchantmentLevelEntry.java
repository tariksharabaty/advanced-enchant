package com.oidioomstudio.advancedenchant.client.gui.screens.inventory;

import com.google.common.collect.ImmutableSet;
import com.oidioomstudio.advancedenchant.client.util.EnchantmentTooltipHelper;
import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public record EnchantmentLevelEntry(int level, InfuserMenu.EnchantmentValues enchantmentValues, Collection<Holder<Enchantment>> incompatibleEnchantments) implements LevelBasedEntry<Enchantment> {
    public static final Component UNKNOWN_ENCHANT_COMPONENT = Component.translatable(
            Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "enchantment.tooltip.unknown_enchantment"))
    ).withStyle(ChatFormatting.GRAY);

    public static EnchantmentLevelEntry create(Holder<Enchantment> enchantment, InfuserMenu.EnchantmentValues enchantmentValues, ItemEnchantments itemEnchantments) {
        int enchantmentLevel = itemEnchantments.getLevel(enchantment);
        HashSet<Holder<Enchantment>> incompatibleEnchantments = new HashSet<>();
        for (Holder<Enchantment> holder : itemEnchantments.keySet()) {
            if (enchantment.equals(holder) || Enchantment.areCompatible(enchantment, holder)) continue;
            incompatibleEnchantments.add(holder);
        }
        return new EnchantmentLevelEntry(enchantmentLevel, enchantmentValues, ImmutableSet.copyOf(incompatibleEnchantments));
    }

    @Override
    public int maxLevel() {
        return this.enchantmentValues.maxLevel();
    }

    @Override
    public int availableLevel() {
        return this.enchantmentValues.availableLevel();
    }

    @Override
    public boolean isIncompatible() {
        return !this.incompatibleEnchantments.isEmpty();
    }

    @Override
    public boolean isInactive() {
        return this.isIncompatible() || LevelBasedEntry.super.isInactive();
    }

    @Override
    public Component getDisplayName(Holder<Enchantment> holder, int maxWidth, int seed) {
        if (this.isNotAvailable()) {
            int enchantmentId = Minecraft.getInstance().getConnection().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getIdOrThrow(holder.value());
            EnchantmentNames.getInstance().initSeed((long) (seed + enchantmentId));
            maxWidth = (int) ((float) maxWidth * 0.72f);
            Font font = Minecraft.getInstance().font;
            FormattedText randomName = EnchantmentNames.getInstance().getRandomName(font, maxWidth);
            return Component.literal(randomName.getString());
        }
        if (this.isPresent()) {
            return EnchantmentTooltipHelper.getDisplayNameWithLevel(holder, this.level);
        }
        return EnchantmentTooltipHelper.getDisplayName(holder);
    }

    @Override
    public List<Component> getTooltip(Holder<Enchantment> holder) {
        if (this.isNotAvailable()) {
            return this.getWeakPowerTooltip(UNKNOWN_ENCHANT_COMPONENT);
        }
        if (this.isIncompatible()) {
            return EnchantmentTooltipHelper.getIncompatibleEnchantmentsTooltip(this.incompatibleEnchantments);
        }
        return EnchantmentTooltipHelper.getEnchantmentTooltip(holder);
    }

    @Override
    public List<Component> getWeakPowerTooltip(Component component) {
        ArrayList<Component> tooltipLines = new ArrayList<>();
        if (this.enchantmentValues.enchantmentPower() > 0 && this.enchantmentValues.requiredEnchantmentPower() > 0) {
            tooltipLines.add(Component.translatable(
                    EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER,
                    this.enchantmentValues.enchantmentPower(),
                    this.enchantmentValues.requiredEnchantmentPower()
            ).withStyle(ChatFormatting.RED));
        }
        tooltipLines.add(component);
        return tooltipLines;
    }
}
