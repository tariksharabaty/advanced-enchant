package com.oidioomstudio.advancedenchant.client.gui.components;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class ScrollingStringWidget extends StringWidget {
    public ScrollingStringWidget(int posX, int posY, int width, int height, Component component, Font font) {
        super(posX, posY, width, height, component, font);
        this.setMaxWidth(width, StringWidget.TextOverflow.SCROLLING);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void visitLines(ActiveTextCollector collector) {
        Component message = this.getMessage();
        Font font = this.getFont();
        int textWidth = font.width(message);
        int availableWidth = this.getWidth();
        int y = this.getY() + (this.getHeight() - 8) / 2;

        if (textWidth > availableWidth) {
            this.extractScrollingStringOverContents(collector, message, 2);
        } else {
            int x = this.getX() + (availableWidth - textWidth) / 2;
            collector.accept(x, y, message.getVisualOrderText());
        }
    }
}
