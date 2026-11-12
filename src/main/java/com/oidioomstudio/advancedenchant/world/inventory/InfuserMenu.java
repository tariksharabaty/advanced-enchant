package com.oidioomstudio.advancedenchant.world.inventory;

import com.oidioomstudio.advancedenchant.config.AdvancedEnchantConfig;
import com.oidioomstudio.advancedenchant.config.ModifiableItems;
import com.oidioomstudio.advancedenchant.init.ModRegistry;
import com.oidioomstudio.advancedenchant.net.ClientboundInfuserEnchantmentsPayload;
import com.oidioomstudio.advancedenchant.net.ServerboundEnchantmentLevelPayload;
import com.oidioomstudio.advancedenchant.util.EnchantmentCostHelper;
import com.oidioomstudio.advancedenchant.util.EnchantmentPowerHelper;
import com.oidioomstudio.advancedenchant.util.ModEnchantmentHelper;
import com.oidioomstudio.advancedenchant.util.PlayerExperienceHelper;
import com.oidioomstudio.advancedenchant.world.item.enchantment.EnchantingBehavior;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserBlock;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class InfuserMenu extends AbstractContainerMenu implements ContainerListener {
    public static final int ENCHANT_BUTTON = 0;
    public static final int REPAIR_BUTTON = 1;
    public static final int ENCHANT_ITEM_SLOT = 0;

    private final InfuserType type;
    private final Container enchantSlots;
    private final ContainerLevelAccess levelAccess;
    private final Player player;
    private final DataSlot enchantmentPower = DataSlot.standalone();
    private final DataSlot enchantingCost = DataSlot.standalone();
    private final DataSlot repairCost = DataSlot.standalone();

    private Object2IntMap<Holder<Enchantment>> enchantmentLevels = Object2IntMaps.emptyMap();
    private Object2IntMap<Holder<Enchantment>> availableEnchantmentLevels = Object2IntMaps.emptyMap();
    private Object2IntMap<Holder<Enchantment>> requiredEnchantmentPowers = Object2IntMaps.emptyMap();
    private int originalEnchantingCost;
    private boolean markedDirty;

    public InfuserMenu(int containerId, Inventory inventory, InfuserType type) {
        this(type, containerId, inventory, new SimpleContainer(1), ContainerLevelAccess.NULL);
    }

    public InfuserMenu(InfuserType type, int containerId, Inventory inventory, Container container, ContainerLevelAccess levelAccess) {
        super(ModRegistry.INFUSING_MENU_TYPE, containerId);
        checkContainerSize(container, 1);
        this.type = type;
        this.enchantSlots = container;
        this.levelAccess = levelAccess;
        this.player = inventory.player;

        // Slot 0: Infuser item
        this.addSlot(new Slot(container, 0, 8, this.getConfig().allowRepairing.isActive() ? 23 : 34) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        EquipmentSlot[] armorSlots = new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        Identifier[] emptySlotTextures = new Identifier[]{
                InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
                InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
                InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
                InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
        };

        // Armor slots (1..4)
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = armorSlots[k];
            Identifier identifier = emptySlotTextures[k];
            this.addSlot(new ArmorSlot(inventory, inventory.player, equipmentSlot, 39 - k, 8 + 188 * (k / 2), 103 + k % 2 * 18, identifier));
        }

        // Standard inventory 3 rows of 9 (slots 5..31)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 30 + col * 18, 103 + row * 18));
            }
        }

        // Hotbar 1 row of 9 (slots 32..40)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 30 + col * 18, 161));
        }

        // Offhand slot (slot 41)
        this.addSlot(new Slot(inventory, 40, 8, 161) {
            @Override
            public void setByPlayer(ItemStack newItemStack, ItemStack oldItemStack) {
                inventory.player.onEquipItem(EquipmentSlot.OFFHAND, oldItemStack, newItemStack);
                super.setByPlayer(newItemStack, oldItemStack);
            }

            @Override
            public Identifier getNoItemIcon() {
                return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            }
        });

        // Elytra / Body slot (slot 42)
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("slotforelytra")) {
            this.addSlot(new ArmorSlot(inventory, inventory.player, EquipmentSlot.BODY, 41, 8, 139, Identifier.fromNamespaceAndPath("slotforelytra", "container/slot/elytra")) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    if (stack.is(Items.ELYTRA) || stack.has(DataComponents.GLIDER) || stack.getItem().toString().toLowerCase().contains("elytra")) {
                        return true;
                    }
                    net.minecraft.world.item.equipment.Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                    return equippable != null && (equippable.slot() == EquipmentSlot.CHEST || equippable.slot() == EquipmentSlot.BODY);
                }
            });
        }

        this.addDataSlot(this.enchantmentPower);
        this.addDataSlot(this.enchantingCost);
        this.addDataSlot(this.repairCost);
    }

    public AdvancedEnchantConfig.InfuserConfig getConfig() {
        return this.type.getConfig();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        super.setItem(slotId, stateId, stack);
        this.broadcastChanges();
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    private boolean lastSlot0HadItem = false;

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack currentStack = this.enchantSlots.getItem(0);
            boolean hasItem = !currentStack.isEmpty();
            if (hasItem && !this.lastSlot0HadItem) {
                this.levelAccess.execute((level, pos) -> {
                    if (this.type == InfuserType.ADVANCED) {
                        level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1.0f, 1.2f);
                    } else {
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.1f);
                    }
                });
            }
            this.lastSlot0HadItem = hasItem;

            this.levelAccess.execute((level, pos) -> {
                this.enchantmentPower.set(this.getAvailablePower(level, pos));
                if (this.mayEnchantStack(this.getEnchantableStack())) {
                    this.setInitialEnchantments(level, Optional.of(this.getOriginalEnchantments()));
                    this.enchantingCost.set(this.calculateEnchantingCost());
                    this.repairCost.set(this.calculateRepairCost());
                } else {
                    this.setInitialEnchantments(level, Optional.empty());
                    this.enchantingCost.set(0);
                    this.repairCost.set(0);
                }
            });
        }
        super.slotsChanged(container);
    }

    private boolean mayEnchantStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (this.getConfig().allowBooks) {
            if (itemStack.is(Items.BOOK)) {
                return true;
            }
            if (itemStack.is(Items.ENCHANTED_BOOK)) {
                return this.getConfig().allowModifyingEnchantments != ModifiableItems.UNENCHANTED;
            }
        } else if (ModEnchantmentHelper.isBook(itemStack)) {
            return false;
        }
        return this.getConfig().allowModifyingEnchantments.predicate.test(itemStack);
    }

    private int getAvailablePower(Level level, BlockPos pos) {
        float enchantingPower = 0.0f;
        float maxPowerScale = 1.0f;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (!InfuserBlock.isValidBookShelf(level, pos, offset)) continue;
            BlockPos targetPos = pos.offset(offset);
            BlockState blockState = level.getBlockState(targetPos);
            enchantingPower += EnchantingBehavior.get().getEnchantmentPower(blockState, level, targetPos);
            maxPowerScale = Math.max(maxPowerScale, EnchantingBehavior.get().getEnchantmentPowerLimitScale(blockState, level, targetPos));
        }
        return (int) Math.min(Math.max(0.0f, enchantingPower), (float) this.getConfig().maximumBookshelves * maxPowerScale);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, ItemStack itemStack) {
        if (containerMenu == this && dataSlotIndex == 0) {
            this.slotsChanged(this.enchantSlots);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
    }

    public boolean clickClientEnchantmentLevelButton(Holder<Enchantment> enchantment, int enchantmentLevel, ServerboundEnchantmentLevelPayload.Operation operation) {
        int newLevel = this.clickEnchantmentLevelButton(enchantment, operation);
        if (newLevel != enchantmentLevel) {
            ClientPlayNetworking.send(new ServerboundEnchantmentLevelPayload(this.containerId, enchantment, operation));
            return true;
        }
        return false;
    }

    public int clickEnchantmentLevelButton(Holder<Enchantment> enchantment, IntUnaryOperator operation) {
        int enchantmentLevel = this.enchantmentLevels.getInt(enchantment);
        if (enchantmentLevel == 0 && !EnchantmentHelper.isEnchantmentCompatible(this.getItemEnchantments().keySet(), enchantment)) {
            return 0;
        }
        int newEnchantmentLevel = operation.applyAsInt(enchantmentLevel);
        newEnchantmentLevel = Mth.clamp(newEnchantmentLevel, 0, EnchantingBehavior.get().getMaxLevel(enchantment));
        newEnchantmentLevel = Math.min(newEnchantmentLevel, this.getAvailableEnchantmentLevel(enchantment));
        if (newEnchantmentLevel != enchantmentLevel) {
            this.enchantmentLevels.put(enchantment, newEnchantmentLevel);
            this.markedDirty = !this.getItemEnchantments().equals(this.getOriginalEnchantments());
            this.enchantingCost.set(this.calculateEnchantingCost());
            this.broadcastChanges();
        }
        return newEnchantmentLevel;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case ENCHANT_BUTTON -> this.clickEnchantButton(player);
            case REPAIR_BUTTON -> this.clickRepairButton(player);
            default -> false;
        };
    }

    private boolean clickEnchantButton(Player player) {
        if (this.canEnchant(player)) {
            this.levelAccess.execute((level, pos) -> {
                this.processEnchantingCost(player, level, pos, this.getEnchantingCost());
                ItemStack itemStack = ModEnchantmentHelper.setNewEnchantments(this.getEnchantableStack(), this.enchantmentLevels, this.getConfig().increaseAnvilRepairCost);
                this.enchantSlots.setItem(0, itemStack);
                if (this.getEnchantingCost() > 0) {
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, itemStack, this.getEnchantingCost());
                    }
                }
                this.enchantSlots.setChanged();
                this.slotsChanged(this.enchantSlots);
                if (this.type == InfuserType.ADVANCED) {
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8f, 1.3f);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.0f);
                }
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
            });
            return true;
        }
        return false;
    }

    private void processEnchantingCost(Player player, Level level, BlockPos blockPos, int enchantingCost) {
        if (enchantingCost < 0) {
            int amount = PlayerExperienceHelper.calculateExperienceDelta(this.getItemEnchantments(), this.getOriginalEnchantments(), level.getRandom());
            if (level instanceof ServerLevel serverLevel) {
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(blockPos.above()), amount);
            }
        } else if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-enchantingCost);
        }
    }

    private int calculateEnchantingCost() {
        int enchantmentCosts = this.getScaledEnchantmentCosts(this.getItemEnchantments()) - this.originalEnchantingCost;
        if (enchantmentCosts == 0 && this.markedDirty) {
            return 1;
        }
        return enchantmentCosts;
    }

    private boolean clickRepairButton(Player player) {
        if (this.canRepair(player)) {
            this.levelAccess.execute((level, pos) -> {
                if (!player.getAbilities().instabuild) {
                    player.giveExperienceLevels(-this.getRepairCost());
                }
                ItemStack itemStack = this.getEnchantableStack();
                int itemRepairCost = itemStack.getOrDefault(DataComponents.REPAIR_COST, 0);
                itemStack = itemStack.copy();
                itemStack.setDamageValue(0);
                if (this.getConfig().increaseAnvilRepairCost) {
                    itemStack.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(itemRepairCost));
                }
                this.enchantSlots.setItem(0, itemStack);
                level.levelEvent(1030, pos, 0);
                if (this.type == InfuserType.ADVANCED) {
                    level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.1f);
                }
            });
            return true;
        }
        return false;
    }

    private int calculateRepairCost() {
        ItemStack itemStack = this.getEnchantableStack();
        if (this.getConfig().allowRepairing.canRepair(itemStack)) {
            double repairStep = (double) itemStack.getMaxDamage() * this.getConfig().repair.repairPercentageStep;
            return (int) Math.ceil(Math.ceil((double) itemStack.getDamageValue() / repairStep) * this.getConfig().repair.repairStepMultiplier);
        }
        return 0;
    }

    private int getScaledEnchantmentCosts(ItemEnchantments itemEnchantments) {
        float enchantmentCostsScale;
        if (ModEnchantmentHelper.isBook(this.getEnchantableStack())) {
            enchantmentCostsScale = 1.0f;
        } else {
            int scalingEnchantmentCosts = EnchantmentCostHelper.getScalingEnchantmentCosts(this.availableEnchantmentLevels.keySet(), this.getScalingNamespaces());
            int maximumCost = Mth.ceil((float) this.getConfig().costs.maximumCost * EnchantingBehavior.get().getMaximumCostMultiplier());
            enchantmentCostsScale = scalingEnchantmentCosts > 0 ? (float) maximumCost / (float) scalingEnchantmentCosts : 1.0f;
        }
        float enchantmentCosts = EnchantmentCostHelper.getEnchantmentCosts(itemEnchantments, enchantmentCostsScale);
        int minimumCosts = itemEnchantments.entrySet().stream().mapToInt(Object2IntMap.Entry::getIntValue).sum();
        return Math.max(Mth.ceil(enchantmentCosts), minimumCosts);
    }

    private Collection<String> getScalingNamespaces() {
        if (this.getConfig().costs.scaleCostsByVanillaOnly) {
            return EnchantingBehavior.get().getScalingNamespaces();
        }
        return Collections.emptySet();
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (slotIndex == 0 && (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE)) {
            Slot slot = this.slots.get(0);
            if (slot.hasItem() && this.getCarried().isEmpty()) {
                ItemStack stack = slot.getItem();
                ItemStack before = stack.copy();
                if (tryAutoEquip(player, stack)) {
                    slot.setByPlayer(ItemStack.EMPTY);
                    slot.onTake(player, before);
                    this.broadcastChanges();
                    return;
                }
            }
        }
        super.clicked(slotIndex, button, input, player);
    }

    private boolean tryAutoEquip(Player player, ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. Check Elytra / Glider
        if (stack.is(Items.ELYTRA) || stack.has(DataComponents.GLIDER) || stack.getItem().toString().toLowerCase().contains("elytra")) {
            // Check custom elytra slot (slot 42) if present
            if (this.slots.size() > 42) {
                Slot elytraSlot = this.slots.get(42);
                if (!elytraSlot.hasItem() && elytraSlot.mayPlace(stack)) {
                    ItemStack toEquip = stack.copy();
                    elytraSlot.setByPlayer(toEquip);
                    stack.shrink(toEquip.getCount());
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_ELYTRA.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    return true;
                }
            }
            // Check chestplate slot (slot 2)
            Slot chestSlot = this.slots.get(2);
            if (!chestSlot.hasItem() && chestSlot.mayPlace(stack)) {
                ItemStack toEquip = stack.copy();
                chestSlot.setByPlayer(toEquip);
                stack.shrink(toEquip.getCount());
                player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_ELYTRA.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }

        // 2. Check standard armor slots (1: HEAD, 2: CHEST, 3: LEGS, 4: FEET)
        EquipmentSlot equipSlot = player.getEquipmentSlotForItem(stack);
        if (equipSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            int slotIdx = switch (equipSlot) {
                case HEAD -> 1;
                case CHEST -> 2;
                case LEGS -> 3;
                case FEET -> 4;
                default -> -1;
            };
            if (slotIdx >= 1 && slotIdx <= 4) {
                Slot armorSlot = this.slots.get(slotIdx);
                if (!armorSlot.hasItem() && armorSlot.mayPlace(stack)) {
                    ItemStack toEquip = stack.copy();
                    armorSlot.setByPlayer(toEquip);
                    stack.shrink(toEquip.getCount());
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tryMoveToArmorOrElytra(ItemStack slotStack) {
        if (slotStack.isEmpty()) return false;
        if (this.slots.size() > 42 && (slotStack.is(Items.ELYTRA) || slotStack.has(DataComponents.GLIDER) || slotStack.getItem().toString().toLowerCase().contains("elytra"))) {
            Slot elytraSlot = this.slots.get(42);
            if (!elytraSlot.hasItem() && elytraSlot.mayPlace(slotStack)) {
                return this.moveItemStackTo(slotStack, 42, 43, false);
            }
        }
        EquipmentSlot equipSlot = this.player.getEquipmentSlotForItem(slotStack);
        if (equipSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            int slotIdx = switch (equipSlot) {
                case HEAD -> 1;
                case CHEST -> 2;
                case LEGS -> 3;
                case FEET -> 4;
                default -> -1;
            };
            if (slotIdx >= 1 && slotIdx <= 4) {
                Slot armorSlot = this.slots.get(slotIdx);
                if (!armorSlot.hasItem() && armorSlot.mayPlace(slotStack)) {
                    return this.moveItemStackTo(slotStack, slotIdx, slotIdx + 1, false);
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index == 0) {
                // Try auto-equipping first if empty
                if (tryAutoEquip(player, slotStack)) {
                    slot.setByPlayer(ItemStack.EMPTY);
                    slot.onTake(player, itemStack);
                    this.broadcastChanges();
                    return itemStack;
                }
                // Infuser slot -> player inventory (slots 1..total)
                if (!this.moveItemStackTo(slotStack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory -> infuser slot or within player inventory
                if (this.mayEnchantStack(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 1 && index <= 4) {
                    // Armor to main inventory
                    if (!this.moveItemStackTo(slotStack, 5, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 5 && index <= 31) {
                    if (!tryMoveToArmorOrElytra(slotStack) && !this.moveItemStackTo(slotStack, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 32 && index <= 40) {
                    if (!tryMoveToArmorOrElytra(slotStack) && !this.moveItemStackTo(slotStack, 5, 32, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index == 41) {
                    // Offhand to main inv
                    if (!this.moveItemStackTo(slotStack, 5, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index == 42) {
                    // Elytra to main inv
                    if (!this.moveItemStackTo(slotStack, 5, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return itemStack;
    }

    public int getEnchantmentPower() {
        return Math.min(this.enchantmentPower.get(), this.getEnchantmentPowerLimit());
    }

    public int getEnchantmentPowerLimit() {
        return EnchantingBehavior.get().getEnchantmentPowerLimit(this.type);
    }

    public ItemStack getEnchantableStack() {
        return this.enchantSlots.getItem(0);
    }

    public int getEnchantingCost() {
        return this.enchantingCost.get();
    }

    public int getRepairCost() {
        return this.repairCost.get();
    }

    public boolean canEnchant(Player player) {
        if (this.getEnchantmentPower() < this.getConfig().minimumBookshelves) {
            return false;
        }
        if (!this.getEnchantableStack().isEmpty() && this.markedDirty) {
            return player.experienceLevel >= this.getEnchantingCost() || player.getAbilities().instabuild;
        }
        return false;
    }

    public boolean canRepair(Player player) {
        if (this.getEnchantmentPower() < this.getConfig().minimumBookshelves) {
            return false;
        }
        if (this.getConfig().allowRepairing.canRepair(this.getEnchantableStack())) {
            return player.experienceLevel >= this.getRepairCost() || player.getAbilities().instabuild;
        }
        return false;
    }

    public ItemEnchantments getItemEnchantments() {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.enchantmentLevels.object2IntEntrySet()) {
            itemEnchantments.set(entry.getKey(), entry.getIntValue());
        }
        return itemEnchantments.toImmutable();
    }

    public Set<Holder<Enchantment>> getAllEnchantments() {
        return this.availableEnchantmentLevels.keySet();
    }

    public int getAvailableEnchantmentLevel(Holder<Enchantment> enchantment) {
        return this.availableEnchantmentLevels.getInt(enchantment);
    }

    private ItemEnchantments getOriginalEnchantments() {
        return EnchantmentHelper.getEnchantmentsForCrafting(this.getEnchantableStack());
    }

    private int getRequiredEnchantmentPower(Holder<Enchantment> enchantment) {
        return this.requiredEnchantmentPowers.getInt(enchantment);
    }

    public void setInitialEnchantments(Level level, Optional<ItemEnchantments> itemEnchantments) {
        this.setItemEnchantments(itemEnchantments.orElse(ItemEnchantments.EMPTY));
        if (itemEnchantments.isPresent()) {
            this.initializeEnchantmentMaps(level);
            this.originalEnchantingCost = this.getScaledEnchantmentCosts(itemEnchantments.get());
        } else {
            this.availableEnchantmentLevels = Object2IntMaps.emptyMap();
            this.requiredEnchantmentPowers = Object2IntMaps.emptyMap();
            this.originalEnchantingCost = 0;
        }
        this.sendEnchantments(itemEnchantments);
    }

    private void setItemEnchantments(ItemEnchantments itemEnchantments) {
        Object2IntOpenHashMap<Holder<Enchantment>> enchantmentLevels = new Object2IntOpenHashMap<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
            enchantmentLevels.put(entry.getKey(), entry.getIntValue());
        }
        this.enchantmentLevels = enchantmentLevels;
        this.markedDirty = false;
    }

    private void initializeEnchantmentMaps(Level level) {
        if (this.getEnchantmentPower() < this.getConfig().minimumBookshelves) {
            this.availableEnchantmentLevels = Object2IntMaps.emptyMap();
            this.requiredEnchantmentPowers = Object2IntMaps.emptyMap();
            return;
        }

        Collection<Holder<Enchantment>> enchantments = ModEnchantmentHelper.getEnchantmentsForItem(
                level.registryAccess(),
                this.getEnchantableStack(),
                this.type.getAvailableEnchantments(),
                !this.getConfig().allowAnvilEnchantments
        );
        int enchantmentValue = this.getEnchantableStack().has(DataComponents.ENCHANTABLE) ?
                this.getEnchantableStack().get(DataComponents.ENCHANTABLE).value() : 0;
        this.availableEnchantmentLevels = EnchantmentPowerHelper.getAvailableEnchantmentLevels(
                this.getEnchantmentPower(),
                enchantments,
                this.getEnchantmentPowerLimit(),
                enchantmentValue
        );
        if (level.isClientSide()) {
            this.requiredEnchantmentPowers = EnchantmentPowerHelper.getRequiredEnchantmentPowers(
                    this.getEnchantmentPower(),
                    enchantments,
                    this.getEnchantmentPowerLimit(),
                    enchantmentValue
            );
        }
    }

    private void sendEnchantments(Optional<ItemEnchantments> itemEnchantments) {
        this.levelAccess.execute((level, blockPos) -> {
            if (this.player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new ClientboundInfuserEnchantmentsPayload(this.containerId, itemEnchantments));
            }
        });
    }

    public EnchantmentValues getEnchantmentValues(Holder<Enchantment> enchantment) {
        int maxLevel = EnchantingBehavior.get().getMaxLevel(enchantment);
        int availableLevel = this.getAvailableEnchantmentLevel(enchantment);
        int requiredEnchantmentPower = this.getRequiredEnchantmentPower(enchantment);
        return new EnchantmentValues(maxLevel, availableLevel, this.getEnchantmentPower(), requiredEnchantmentPower);
    }

    public record EnchantmentValues(int maxLevel, int availableLevel, int enchantmentPower, int requiredEnchantmentPower) {
    }
}
