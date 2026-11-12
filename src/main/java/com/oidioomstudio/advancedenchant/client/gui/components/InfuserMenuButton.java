package com.oidioomstudio.advancedenchant.client.gui.components;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InfuserMenuButton extends ImageButton {
    public static final String KEY_TOOLTIP_EXPERIENCE = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "infusing.tooltip.points"));
    public static final String KEY_TOOLTIP_CHANGE = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "infusing.tooltip.change"));
    public static final String KEY_TOOLTIP_DURABILITY = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "infusing.tooltip.durability"));
    private Component backdropMessage = CommonComponents.EMPTY;

    public InfuserMenuButton(int x, int y, WidgetSprites widgetSprites, Button.OnPress onPress) {
        super(x, y, 18, 18, widgetSprites, onPress);
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.backdropMessage = message.copy().withStyle(ChatFormatting.BLACK);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabel(guiGraphics);
    }

    protected void renderLabel(GuiGraphicsExtractor guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int posX = this.getX() + this.getWidth() - font.width(this.getMessage());
        int posY = this.getY() + this.getHeight() - 9 + 1;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                guiGraphics.text(font, this.backdropMessage, posX + i, posY + j, -1, false);
            }
        }
        guiGraphics.text(font, this.getMessage(), posX, posY, -1, false);
    }

    public void refreshMessage(int value, boolean mayApply) {
        if (value != 0) {
            ChatFormatting chatFormatting = this.getStringColor(value, mayApply);
            MutableComponent component = Component.literal(this.getStringValue(value)).withStyle(chatFormatting);
            this.setMessage(component);
        } else {
            this.setMessage(CommonComponents.EMPTY);
        }
    }

    abstract ChatFormatting getStringColor(int value, boolean mayApply);

    abstract String getStringValue(int value);

    public void refreshTooltip(ItemStack itemStack, ItemEnchantments itemEnchantments, int value, boolean mayApply) {
        List<FormattedText> tooltipLines = this.getTooltipLines(itemStack, itemEnchantments, value, mayApply);
        if (tooltipLines.isEmpty()) {
            this.setTooltip(null);
        } else {
            MutableComponent comp = Component.empty();
            for (int i = 0; i < tooltipLines.size(); i++) {
                if (i > 0) comp.append(CommonComponents.NEW_LINE);
                comp.append((Component) tooltipLines.get(i));
            }
            this.setTooltip(Tooltip.create(comp));
        }
    }

    private List<FormattedText> getTooltipLines(ItemStack itemStack, ItemEnchantments itemEnchantments, int value, boolean mayApply) {
        if (!mayApply && value == 0) {
            return Collections.emptyList();
        }
        ArrayList<FormattedText> lines = new ArrayList<>();
        if (mayApply) {
            lines.add(this.getNameComponent(itemStack, itemEnchantments));
            lines.addAll(this.getCustomLines(itemStack, itemEnchantments));
        }
        Component levelsComponent = this.getLevelsComponent(value, mayApply);
        if (levelsComponent != null) {
            if (!lines.isEmpty()) {
                lines.add(CommonComponents.EMPTY);
            }
            lines.add(levelsComponent);
        }
        return lines;
    }

    abstract List<FormattedText> getCustomLines(ItemStack itemStack, ItemEnchantments itemEnchantments);

    private @Nullable Component getLevelsComponent(int value, boolean mayApply) {
        if (value < 0) {
            return Component.translatable(KEY_TOOLTIP_EXPERIENCE).withStyle(ChatFormatting.GREEN);
        }
        if (value > 0) {
            ChatFormatting formatting = mayApply ? ChatFormatting.GREEN : ChatFormatting.RED;
            return Component.translatable("container.repair.cost", value).withStyle(formatting);
        }
        return null;
    }

    protected Component getNameComponent(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        Rarity rarity = itemStack.getRarity();
        return Component.empty().append(itemStack.getHoverName()).withStyle(rarity.color());
    }
}
