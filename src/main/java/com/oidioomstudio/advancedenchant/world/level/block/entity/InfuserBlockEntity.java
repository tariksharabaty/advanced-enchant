package com.oidioomstudio.advancedenchant.world.level.block.entity;

import com.oidioomstudio.advancedenchant.init.ModRegistry;
import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserBlock;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserType;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class InfuserBlockEntity extends BlockEntity implements Nameable, WorldlyContainer, ExtendedMenuProvider<InfuserType> {
    private static final RandomSource RANDOM = RandomSource.create();

    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    private @Nullable Component name;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private LockCode lockKey = LockCode.NO_LOCK;

    public InfuserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.INFUSER_BLOCK_ENTITY_TYPE;
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.name = parseCustomNameSafe(valueInput, "CustomName");
        this.lockKey = LockCode.fromTag(valueInput);
        this.items.clear();
        ContainerHelper.loadAllItems(valueInput, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable("CustomName", net.minecraft.network.chat.ComponentSerialization.CODEC, this.name);
        this.lockKey.addToTag(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items, true);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, InfuserBlockEntity blockEntity) {
        blockEntity.oOpen = blockEntity.open;
        blockEntity.oRot = blockEntity.rot;
        Player player = level.getNearestPlayer(
                (double) blockPos.getX() + 0.5,
                (double) blockPos.getY() + 0.5,
                (double) blockPos.getZ() + 0.5,
                3.0,
                false
        );
        if (player != null) {
            double d0 = player.getX() - ((double) blockPos.getX() + 0.5);
            double d1 = player.getZ() - ((double) blockPos.getZ() + 0.5);
            blockEntity.tRot = (float) Mth.atan2(d1, d0);
            blockEntity.open += 0.1f;
            if (blockEntity.open < 0.5f || RANDOM.nextInt(40) == 0) {
                float f1 = blockEntity.flipT;
                do {
                    blockEntity.flipT += (float) (RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f1 == blockEntity.flipT);
            }
        } else {
            blockEntity.tRot += 0.02f;
            blockEntity.open -= 0.1f;
        }

        while (blockEntity.rot >= (float) Math.PI) {
            blockEntity.rot -= (float) (Math.PI * 2);
        }
        while (blockEntity.rot < -(float) Math.PI) {
            blockEntity.rot += (float) (Math.PI * 2);
        }
        while (blockEntity.tRot >= (float) Math.PI) {
            blockEntity.tRot -= (float) (Math.PI * 2);
        }
        while (blockEntity.tRot < -(float) Math.PI) {
            blockEntity.tRot += (float) (Math.PI * 2);
        }

        float f2 = blockEntity.tRot - blockEntity.rot;
        while (f2 >= (float) Math.PI) {
            f2 -= (float) (Math.PI * 2);
        }
        while (f2 < -(float) Math.PI) {
            f2 += (float) (Math.PI * 2);
        }

        blockEntity.rot += f2 * 0.4f;
        blockEntity.open = Mth.clamp(blockEntity.open, 0.0f, 1.0f);
        blockEntity.time++;
        blockEntity.oFlip = blockEntity.flip;
        float f = (blockEntity.flipT - blockEntity.flip) * 0.4f;
        f = Mth.clamp(f, -0.2f, 0.2f);
        blockEntity.flipA += (f - blockEntity.flipA) * 0.9f;
        blockEntity.flip += blockEntity.flipA;
    }

    public InfuserType getInfuserType() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof InfuserBlock infuserBlock) {
            return infuserBlock.getType();
        }
        return InfuserType.NORMAL;
    }

    @Override
    public InfuserType getScreenOpeningData(ServerPlayer player) {
        return this.getInfuserType();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (this.canOpen(player)) {
            InfuserMenu infuserMenu = new InfuserMenu(
                    this.getInfuserType(),
                    containerId,
                    inventory,
                    this,
                    ContainerLevelAccess.create(this.getLevel(), this.getBlockPos())
            );
            infuserMenu.addSlotListener(infuserMenu);
            return infuserMenu;
        }
        BaseContainerBlockEntity.sendChestLockedNotifications(Vec3.atCenterOf(this.getBlockPos()), player, this.getDisplayName());
        return null;
    }

    public boolean canOpen(Player player) {
        return this.lockKey.canUnlock(player);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        if (index == 0) {
            return this.getItem(0).isEmpty();
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return index == 0 && (itemStack.isEnchanted() || itemStack.is(Items.ENCHANTED_BOOK));
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return Component.translatable("container.enchant");
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.name;
    }

    public void setCustomName(@Nullable Component name) {
        this.name = name;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter getter) {
        super.applyImplicitComponents(getter);
        this.name = getter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("CustomName");
    }
}
