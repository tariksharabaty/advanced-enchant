package com.oidioomstudio.advancedenchant.client.gui.components;

import com.google.common.collect.Sets;
import com.oidioomstudio.advancedenchant.client.gui.screens.inventory.InfuserScreen;
import com.oidioomstudio.advancedenchant.util.ModEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InfuserEnchantButton extends InfuserMenuButton {
    public InfuserEnchantButton(int x, int y, Button.OnPress onPress) {
        super(x, y, InfuserScreen.ENCHANT_BUTTON_SPRITES, onPress);
    }

    @Override
    ChatFormatting getStringColor(int value, boolean mayApply) {
        if (value < 0) {
            return ChatFormatting.AQUA;
        }
        return mayApply ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    @Override
    String getStringValue(int value) {
        return value < 0 ? "+" + -value : String.valueOf(value);
    }

    @Override
    protected Component getNameComponent(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        ItemStack newItemStack = ModEnchantmentHelper.getEnchantedItemStack(itemStack, !itemEnchantments.isEmpty());
        return Component.empty().append(newItemStack.getHoverName()).withStyle(ModEnchantmentHelper.getItemNameRarity(itemStack, itemEnchantments).color());
    }

    @Override
    List<FormattedText> getCustomLines(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        ArrayList<FormattedText> lines = new ArrayList<>();
        ItemEnchantments oldEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        Registry<Enchantment> registry = Minecraft.getInstance().getConnection().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Optional<HolderSet.Named<Enchantment>> tooltipsTag = registry.get(EnchantmentTags.TOOLTIP_ORDER);
        Set<Holder<Enchantment>> combined = Sets.union(oldEnchantments.keySet(), itemEnchantments.keySet());
        for (Holder<Enchantment> holder : tooltipsTag.isPresent() ? tooltipsTag.get().stream().filter(combined::contains).toList() : combined) {
            int oldLevel = oldEnchantments.getLevel(holder);
            int newLevel = itemEnchantments.getLevel(holder);
            if (oldLevel == newLevel) continue;
            Component changeComponent = this.getChangeComponent(holder, oldLevel, newLevel);
            lines.add(changeComponent);
        }
        return lines;
    }

    private Component getChangeComponent(Holder<Enchantment> holder, int oldLevel, int newLevel) {
        Component enchantmentName = Enchantment.getFullname(holder, Math.max(oldLevel, newLevel));
        if (oldLevel == 0) {
            return Component.literal("+ ").append(enchantmentName).withStyle(ChatFormatting.AQUA);
        }
        if (newLevel == 0) {
            return Component.literal("- ").append(enchantmentName).withStyle(ChatFormatting.RED);
        }
        MutableComponent change = Component.translatable(KEY_TOOLTIP_CHANGE, Component.translatable("enchantment.level." + oldLevel), Component.translatable("enchantment.level." + newLevel));
        return ((Enchantment) holder.value()).description().copy().append(CommonComponents.SPACE).append(change).withStyle(ChatFormatting.YELLOW);
    }
}
