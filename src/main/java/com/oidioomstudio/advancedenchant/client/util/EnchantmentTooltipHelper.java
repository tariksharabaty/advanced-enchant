package com.oidioomstudio.advancedenchant.client.util;

import com.oidioomstudio.advancedenchant.world.item.enchantment.EnchantingBehavior;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EnchantmentTooltipHelper {
    public static final String KEY_INCOMPATIBLE_ENCHANTMENTS = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "enchantment.tooltip.incompatible"));
    public static final String KEY_CURRENT_ENCHANTING_POWER = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "enchantment.tooltip.current_enchanting_power"));

    public static List<Component> getIncompatibleEnchantmentsTooltip(Collection<Holder<Enchantment>> incompatibleEnchantments) {
        MutableComponent listComp = Component.empty();
        boolean first = true;
        for (Holder<Enchantment> holder : incompatibleEnchantments) {
            if (!first) {
                listComp.append(", ");
            }
            listComp.append(getDisplayName(holder));
            first = false;
        }
        MutableComponent component = Component.translatable(KEY_INCOMPATIBLE_ENCHANTMENTS, listComp.withStyle(ChatFormatting.GRAY));
        return Collections.singletonList(component);
    }

    public static List<Component> getEnchantmentTooltip(Holder<Enchantment> holder) {
        ArrayList<Component> tooltipLines = new ArrayList<>();
        Component levelComponent = getLevelComponent(holder);
        tooltipLines.add(holder.value().description().copy().append(CommonComponents.SPACE).append(levelComponent));
        String translationKey = getEnchantmentDescriptionKey(holder);
        if (translationKey != null) {
            tooltipLines.add(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));
        }
        return tooltipLines;
    }

    private static Component getLevelComponent(Holder<Enchantment> holder) {
        int minLevel = holder.value().getMinLevel();
        int maxLevel = EnchantingBehavior.get().getMaxLevel(holder);
        MutableComponent component = Component.translatable("enchantment.level." + minLevel);
        if (minLevel != maxLevel) {
            component.append(" - ").append(Component.translatable("enchantment.level." + maxLevel));
        }
        return component;
    }

    private static @Nullable String getEnchantmentDescriptionKey(Holder<Enchantment> holder) {
        String descriptionKey = holder.unwrapKey().orElseThrow().identifier().toLanguageKey("enchantment") + ".desc";
        if (Language.getInstance().has(descriptionKey)) {
            return descriptionKey;
        }
        return null;
    }

    public static Component getDisplayName(Holder<Enchantment> holder) {
        return holder.value().description().copy();
    }

    public static Component getDisplayNameWithLevel(Holder<Enchantment> holder, int level) {
        return Enchantment.getFullname(holder, level);
    }
}
