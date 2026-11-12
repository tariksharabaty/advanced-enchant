package com.oidioomstudio.advancedenchant.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;

import com.oidioomstudio.advancedenchant.client.gui.screens.inventory.InfuserScreen;
import com.oidioomstudio.advancedenchant.client.gui.screens.inventory.LevelBasedEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.List;

public abstract class LevelBasedOperationButton extends ImageButton {
    public static final Component INCREASE_LEVEL_COMPONENT = Component.translatable(
            Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "enchantment.tooltip.low_power1"))
    ).withStyle(ChatFormatting.GRAY);

    public static final Component MODIFY_LEVEL_COMPONENT = Component.translatable(
            Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "enchantment.tooltip.low_power2"))
    ).withStyle(ChatFormatting.GRAY);

        public static boolean isShiftKeyDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }
    private final boolean isPowerTooLow;

    public LevelBasedOperationButton(LevelBasedEntry<?> levelBasedEntry, int x, int y, WidgetSprites widgetSprites, Button.OnPress onPress, Component component) {
        super(x, y, 18, 18, widgetSprites, onPress);
        this.visible = true;
        this.active = this.getActiveValue(levelBasedEntry);
        if (this.isPowerLevelSufficient(levelBasedEntry)) {
            this.isPowerTooLow = true;
            List<Component> weakLines = levelBasedEntry.getWeakPowerTooltip(component);
            MutableComponent comp = Component.empty();
            for (int i = 0; i < weakLines.size(); i++) {
                if (i > 0) comp.append(CommonComponents.NEW_LINE);
                comp.append(weakLines.get(i));
            }
            this.setTooltip(Tooltip.create(comp));
        } else {
            this.isPowerTooLow = false;
        }
    }

    protected abstract boolean getActiveValue(LevelBasedEntry<?> entry);

    protected abstract boolean isPowerLevelSufficient(LevelBasedEntry<?> entry);

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        if (this.isActive() && isShiftKeyDown()) {
            Identifier identifier = this.sprites.get(true, this.isHoveredOrFocused());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX() - 3, this.getY(), this.width, this.height);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX() + 3, this.getY(), this.width, this.height);
        } else {
            super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (this.isPowerTooLow && this.isHoveredOrFocused()) {
            InfuserScreen.setIsPowerTooLow(true);
        }
    }

    public static class Remove extends LevelBasedOperationButton {
        public Remove(LevelBasedEntry<?> levelBasedEntry, int x, int y, Button.OnPress onPress) {
            super(levelBasedEntry, x, y, InfuserScreen.REMOVE_BUTTON_SPRITES, onPress, MODIFY_LEVEL_COMPONENT);
        }

        @Override
        protected boolean getActiveValue(LevelBasedEntry<?> levelBasedEntry) {
            return levelBasedEntry.isPresent() && !levelBasedEntry.isIncompatible() && !levelBasedEntry.isNotAvailable() && levelBasedEntry.level() - 1 < levelBasedEntry.availableLevel();
        }

        @Override
        protected boolean isPowerLevelSufficient(LevelBasedEntry<?> levelBasedEntry) {
            return levelBasedEntry.isPresent() && levelBasedEntry.level() - 1 >= levelBasedEntry.availableLevel() && !levelBasedEntry.isNotAvailable();
        }
    }

    public static class Add extends LevelBasedOperationButton {
        public Add(LevelBasedEntry<?> levelBasedEntry, int x, int y, Button.OnPress onPress) {
            super(levelBasedEntry, x, y, InfuserScreen.ADD_BUTTON_SPRITES, onPress, INCREASE_LEVEL_COMPONENT);
        }

        @Override
        protected boolean getActiveValue(LevelBasedEntry<?> levelBasedEntry) {
            return levelBasedEntry.level() < levelBasedEntry.maxLevel() && !levelBasedEntry.isIncompatible() && !levelBasedEntry.isNotAvailable() && levelBasedEntry.level() < levelBasedEntry.availableLevel();
        }

        @Override
        protected boolean isPowerLevelSufficient(LevelBasedEntry<?> levelBasedEntry) {
            return levelBasedEntry.level() < levelBasedEntry.maxLevel() && levelBasedEntry.level() >= levelBasedEntry.availableLevel() && !levelBasedEntry.isNotAvailable();
        }
    }
}
