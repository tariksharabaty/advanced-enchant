package com.oidioomstudio.advancedenchant.client.gui.components;

import com.oidioomstudio.advancedenchant.client.gui.screens.inventory.InfuserScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Collections;
import java.util.List;

public class InfuserRepairButton extends InfuserMenuButton {
    public InfuserRepairButton(int x, int y, Button.OnPress onPress) {
        super(x, y, InfuserScreen.REPAIR_BUTTON_SPRITES, onPress);
    }

    @Override
    ChatFormatting getStringColor(int value, boolean mayApply) {
        return mayApply ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    @Override
    String getStringValue(int value) {
        return String.valueOf(value);
    }

    @Override
    List<FormattedText> getCustomLines(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        MutableComponent changeComponent = Component.translatable(KEY_TOOLTIP_CHANGE, itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage());
        MutableComponent durabilityComponent = Component.translatable(KEY_TOOLTIP_DURABILITY, changeComponent).withStyle(ChatFormatting.YELLOW);
        return Collections.singletonList(durabilityComponent);
    }
}
