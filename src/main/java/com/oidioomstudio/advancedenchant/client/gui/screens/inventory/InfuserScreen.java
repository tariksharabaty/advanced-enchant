package com.oidioomstudio.advancedenchant.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.oidioomstudio.advancedenchant.client.gui.components.*;
import com.oidioomstudio.advancedenchant.client.util.EnchantmentTooltipHelper;
import com.oidioomstudio.advancedenchant.net.ServerboundEnchantmentLevelPayload;
import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> implements ContainerListener {
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("advancedenchant", "textures/gui/container/infuser.png");
    public static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/button"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/button_disabled"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/button_highlighted")
    );
    public static final WidgetSprites ENCHANT_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/enchant_button"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/enchant_button_disabled"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/enchant_button_highlighted")
    );
    public static final WidgetSprites REPAIR_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/repair_button"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/repair_button_disabled"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/repair_button_highlighted")
    );
    public static final WidgetSprites REMOVE_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/remove_button"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/remove_button_disabled"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/remove_button_highlighted")
    );
    public static final WidgetSprites ADD_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/add_button"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/add_button_disabled"),
            Identifier.fromNamespaceAndPath("advancedenchant", "container/infuser/add_button_highlighted")
    );

    public static final String KEY_TOOLTIP_HINT = Util.makeDescriptionId("gui", Identifier.fromNamespaceAndPath("advancedenchant", "infusing.tooltip.enchanting_power_hint"));
    private static boolean isPowerTooLow;
    final int enchantmentSeed = new Random().nextInt();
    private EditBox searchBox;
    private EnchantmentSelectionList scrollingList;
    private boolean ignoreTextInput;
    private AbstractWidget powerWidget;
    private InfuserMenuButton enchantButton;
    private @Nullable InfuserMenuButton repairButton;

    public InfuserScreen(InfuserMenu infuserMenu, Inventory inventory, Component title) {
        super(infuserMenu, inventory, title, 220, 185);
        this.inventoryLabelX = 30;
        this.inventoryLabelY = this.imageHeight - 94;
        this.menu.addSlotListener(this);
    }

    public static void setIsPowerTooLow(boolean isPowerTooLow) {
        InfuserScreen.isPowerTooLow = isPowerTooLow;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(this.font, this.leftPos + 67, this.topPos + 6, 116, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(-1);
        this.searchBox.setInvertHighlightedTextColor(false);
        this.addRenderableWidget(this.searchBox);

        this.scrollingList = new EnchantmentSelectionList(this, this.leftPos + 30, this.topPos + 18);
        this.addRenderableWidget(this.scrollingList);

        this.powerWidget = this.addRenderableOnly(new ItemStackDisplayWidget(this.leftPos + 196, this.topPos + 161, this.font, new ItemStack(Items.BOOKSHELF)) {
            private Component maximumEnchantmentPowerMessage = CommonComponents.EMPTY;
            private Component powerIsTooLowMessage = CommonComponents.EMPTY;

            @Override
            public Component getMessage() {
                if (InfuserScreen.this.menu.getEnchantmentPower() >= InfuserScreen.this.menu.getEnchantmentPowerLimit()) {
                    return this.maximumEnchantmentPowerMessage;
                }
                if (isPowerTooLow) {
                    return this.powerIsTooLowMessage;
                }
                return super.getMessage();
            }

            @Override
            public void setMessage(Component message) {
                super.setMessage(message);
                this.maximumEnchantmentPowerMessage = ComponentUtils.mergeStyles(message, Style.EMPTY.withColor(ChatFormatting.YELLOW));
                this.powerIsTooLowMessage = ComponentUtils.mergeStyles(message, Style.EMPTY.withColor(ChatFormatting.RED));
            }
        });

        this.enchantButton = this.addRenderableWidget(new InfuserEnchantButton(
                this.leftPos + 7,
                this.topPos + (this.menu.getConfig().allowRepairing.isActive() ? 44 : 55),
                button -> {
                    if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.ENCHANT_BUTTON)) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, InfuserMenu.ENCHANT_BUTTON);
                    }
                    this.searchBox.setValue("");
                }
        ));

        this.repairButton = this.menu.getConfig().allowRepairing.isActive() ? this.addRenderableWidget(new InfuserRepairButton(
                this.leftPos + 7,
                this.topPos + 66,
                button -> {
                    if (this.menu.clickMenuButton(this.minecraft.player, InfuserMenu.REPAIR_BUTTON)) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, InfuserMenu.REPAIR_BUTTON);
                    }
                }
        )) : null;

        this.refreshAllButtons();
    }

    private void refreshAllButtons() {
        this.refreshButton(0);
        this.refreshButton(1);
        this.refreshButton(2);
    }

    private void refreshButton(int dataSlot) {
        switch (dataSlot) {
            case 0 -> this.refreshEnchantingPower(this.menu.getEnchantmentPower());
            case 1 -> this.refreshButton(this.enchantButton, this.menu.getEnchantingCost(), this.menu.canEnchant(this.minecraft.player));
            case 2 -> {
                if (this.repairButton != null) {
                    this.refreshButton(this.repairButton, this.menu.getRepairCost(), this.menu.canRepair(this.minecraft.player));
                }
            }
        }
    }

    private void refreshButton(InfuserMenuButton button, int value, boolean mayApply) {
        button.refreshMessage(value, mayApply);
        button.refreshTooltip(this.menu.getEnchantableStack(), this.menu.getItemEnchantments(), value, mayApply);
        button.active = mayApply;
    }

    private void refreshEnchantingPower(int enchantmentPower) {
        this.powerWidget.setMessage(Component.literal(String.valueOf(enchantmentPower)));
        int enchantmentPowerLimit = this.menu.getEnchantmentPowerLimit();
        MutableComponent comp = Component.translatable(EnchantmentTooltipHelper.KEY_CURRENT_ENCHANTING_POWER, enchantmentPower, enchantmentPowerLimit).withStyle(ChatFormatting.GREEN);
        if (enchantmentPower < enchantmentPowerLimit) {
            comp.append(CommonComponents.NEW_LINE).append(Component.translatable(KEY_TOOLTIP_HINT).withStyle(ChatFormatting.GRAY));
        }
        this.powerWidget.setTooltip(Tooltip.create(comp));
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void resize(int width, int height) {
        String string = this.searchBox.getValue();
        super.resize(width, height);
        this.searchBox.setValue(string);
        this.refreshSearchResults();
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (this.ignoreTextInput) {
            return false;
        }
        String s = this.searchBox.getValue();
        if (this.searchBox.charTyped(characterEvent)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        this.ignoreTextInput = false;
        if (!this.searchBox.isFocused()) {
            if (this.minecraft.options.keyChat.matches(keyEvent)) {
                this.ignoreTextInput = true;
                this.searchBox.setFocused(true);
                return true;
            }
            return super.keyPressed(keyEvent);
        }
        boolean flag = this.hoveredSlot != null && this.hoveredSlot.hasItem();
        boolean flag1 = InputConstants.getKey(keyEvent).getNumericKeyValue().isPresent();
        if (flag && flag1 && this.checkHotbarKeyPressed(keyEvent)) {
            this.ignoreTextInput = true;
            return true;
        }
        String s = this.searchBox.getValue();
        if (this.searchBox.keyPressed(keyEvent)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return (this.searchBox.isFocused() && this.searchBox.isVisible() && !keyEvent.isEscape()) || super.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        this.ignoreTextInput = false;
        return super.keyReleased(keyEvent);
    }

    public void refreshSearchResults() {
        int size = this.scrollingList.children().size();
        this.scrollingList.clearList();
        ItemEnchantments itemEnchantments = this.menu.getItemEnchantments();
        Set<Holder<Enchantment>> allEnchantments = this.menu.getAllEnchantments();
        RegistryAccess registries = this.minecraft.getConnection().registryAccess();
        Optional<HolderSet.Named<Enchantment>> sortedOptional = registries.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
        List<Holder<Enchantment>> sortedList = sortedOptional.map(HolderSet.Named::stream).map(s -> s.toList()).orElse(Collections.emptyList());

        for (Holder<Enchantment> enchantment : sortedList) {
            if (!allEnchantments.contains(enchantment) || !this.matchesSearch(enchantment)) continue;
            InfuserMenu.EnchantmentValues enchantmentValues = this.menu.getEnchantmentValues(enchantment);
            EnchantmentLevelEntry entry = EnchantmentLevelEntry.create(enchantment, enchantmentValues, itemEnchantments);
            this.scrollingList.addEntry(enchantment, entry);
        }
        for (Holder<Enchantment> enchantment : allEnchantments) {
            if (sortedList.contains(enchantment) || !this.matchesSearch(enchantment)) continue;
            InfuserMenu.EnchantmentValues enchantmentValues = this.menu.getEnchantmentValues(enchantment);
            EnchantmentLevelEntry entry = EnchantmentLevelEntry.create(enchantment, enchantmentValues, itemEnchantments);
            this.scrollingList.addEntry(enchantment, entry);
        }
        if (size != this.scrollingList.children().size()) {
            this.scrollingList.setScrollAmount(0.0);
        }
    }

    private boolean matchesSearch(Holder<Enchantment> holder) {
        String searchQuery = this.searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        if (searchQuery.isEmpty()) {
            return true;
        }
        if (this.menu.getAvailableEnchantmentLevel(holder) == 0) {
            return false;
        }
        return holder.value().description().getString().toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        isPowerTooLow = false;
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (mouseX >= this.leftPos + this.titleLabelX && mouseX <= this.leftPos + 64 &&
                mouseY >= this.topPos + this.titleLabelY && mouseY <= this.topPos + this.titleLabelY + 10) {
            guiGraphics.setTooltipForNextFrame(this.font, this.title, mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int maxTitleWidth = 56;
        Component titleToRender = this.title;
        if (this.font.width(titleToRender) > maxTitleWidth) {
            String truncated = this.font.plainSubstrByWidth(titleToRender.getString(), maxTitleWidth - 6) + "...";
            titleToRender = Component.literal(truncated);
        }
        guiGraphics.text(this.font, titleToRender, this.titleLabelX, this.titleLabelY, -12566464, false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.scrollingList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.scrollingList.mouseDragged(event, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (this.scrollingList.mouseReleased(event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        Slot slot = this.menu.getSlot(0);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 18, 18);
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("slotforelytra")) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, this.leftPos + 7, this.topPos + 138, 18, 18);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (dataSlotIndex == 0) {
            this.refreshButton(1);
            this.refreshButton(2);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        this.refreshButton(dataSlotIndex);
    }

    public static class EnchantmentSelectionList extends ContainerObjectSelectionList<EnchantmentSelectionList.Entry> {
        private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
        private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
        final InfuserScreen screen;

        public EnchantmentSelectionList(InfuserScreen screen, int x, int y) {
            super(screen.minecraft, 160, 70, y, 18);
            this.screen = screen;
            this.setX(x);
        }

        public void clearList() {
            this.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return 160;
        }

        @Override
        protected int scrollerHeight() {
            return 15;
        }

        @Override
        public int scrollbarWidth() {
            return 12;
        }

        @Override
        protected int scrollBarX() {
            return this.screen.leftPos + 198;
        }

        @Override
        protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            Identifier sprite = this.scrollable() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
            int y = this.scrollable() ? this.scrollBarY() : this.getY();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.scrollBarX(), y, 12, 15);
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor extractor) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor extractor) {
        }

        public void addEntry(Holder<Enchantment> holder, LevelBasedEntry<Enchantment> levelBasedEntry) {
            this.addEntry(new Entry(this, holder, levelBasedEntry));
        }

        public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            private final Identifier backgroundSprite;
            private final Identifier sideSprite;
            private final boolean isPowerTooLow;
            private final ScrollingStringWidget stringWidget;
            private final LevelBasedOperationButton.Remove removeButton;
            private final LevelBasedOperationButton.Add addButton;
            private final List<AbstractWidget> children = new ArrayList<>();

            public Entry(EnchantmentSelectionList list, Holder<Enchantment> holder, LevelBasedEntry<Enchantment> levelBasedEntry) {
                this.backgroundSprite = BUTTON_SPRITES.get(!levelBasedEntry.isInactive(), levelBasedEntry.isPresent());
                this.sideSprite = BUTTON_SPRITES.get(!levelBasedEntry.isInactive(), false);
                this.isPowerTooLow = levelBasedEntry.isNotAvailable();
                Component component = ComponentUtils.mergeStyles(
                        levelBasedEntry.getDisplayName(holder, list.getWidth() - 36, list.screen.enchantmentSeed),
                        this.getStyle(levelBasedEntry)
                );
                this.stringWidget = new ScrollingStringWidget(list.getX() + 18, list.getY(), list.getWidth() - 36, 18, component, list.screen.font);

                List<Component> tooltip = levelBasedEntry.getTooltip(holder);
                if (!tooltip.isEmpty()) {
                    MutableComponent comp = Component.empty();
                    for (int i = 0; i < tooltip.size(); i++) {
                        if (i > 0) comp.append(CommonComponents.NEW_LINE);
                        comp.append(tooltip.get(i));
                    }
                    this.stringWidget.setTooltip(Tooltip.create(comp));
                }

                this.removeButton = new LevelBasedOperationButton.Remove(levelBasedEntry, list.getX(), list.getY(), button -> {
                    if (list.screen.menu.clickClientEnchantmentLevelButton(holder, levelBasedEntry.level(), ServerboundEnchantmentLevelPayload.Operation.remove(LevelBasedOperationButton.isShiftKeyDown()))) {
                        list.screen.refreshSearchResults();
                    }
                });

                this.addButton = new LevelBasedOperationButton.Add(levelBasedEntry, list.getX() + list.getWidth() - 18, list.getY(), button -> {
                    if (list.screen.menu.clickClientEnchantmentLevelButton(holder, levelBasedEntry.level(), ServerboundEnchantmentLevelPayload.Operation.add(LevelBasedOperationButton.isShiftKeyDown()))) {
                        list.screen.refreshSearchResults();
                    }
                });

                this.children.add(this.removeButton);
                this.children.add(this.stringWidget);
                this.children.add(this.addButton);
            }

            @Override
            public int getContentX() {
                return this.getX();
            }

            @Override
            public int getContentY() {
                return this.getY();
            }

            @Override
            public int getContentWidth() {
                return this.getWidth();
            }

            @Override
            public int getContentHeight() {
                return this.getHeight();
            }

            private Style getStyle(LevelBasedEntry<?> levelBasedEntry) {
                if (levelBasedEntry.isInactive()) {
                    return Style.EMPTY.withColor(6839882);
                }
                if (levelBasedEntry.isPresent()) {
                    return Style.EMPTY.withColor(ChatFormatting.YELLOW);
                }
                return Style.EMPTY;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
                int x = this.getX();
                int y = this.getY();
                int width = this.getWidth();
                int height = this.getHeight();

                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sideSprite, 160, 18, 0, 0, x, y, 18, 18);
                this.removeButton.setX(x);
                this.removeButton.setY(y);
                this.removeButton.extractContents(guiGraphics, mouseX, mouseY, partialTick);

                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.backgroundSprite, 160, 18, 18, 0, x + 18, y, width - 36, 18);

                this.stringWidget.setX(x + 18);
                this.stringWidget.setY(y);
                this.stringWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sideSprite, 160, 18, 142, 0, x + width - 18, y, 18, 18);
                this.addButton.setX(x + width - 18);
                this.addButton.setY(y);
                this.addButton.extractContents(guiGraphics, mouseX, mouseY, partialTick);

                if (hovering && this.isPowerTooLow) {
                    InfuserScreen.setIsPowerTooLow(true);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            @Override
            public void visitWidgets(Consumer<AbstractWidget> consumer) {
                this.children.forEach(consumer);
            }
        }
    }
}
