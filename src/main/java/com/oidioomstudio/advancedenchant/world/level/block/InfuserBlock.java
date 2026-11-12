package com.oidioomstudio.advancedenchant.world.level.block;

import com.oidioomstudio.advancedenchant.init.ModRegistry;
import com.oidioomstudio.advancedenchant.world.item.enchantment.EnchantingBehavior;
import com.oidioomstudio.advancedenchant.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class InfuserBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    private final InfuserType type;

    public InfuserBlock(InfuserType type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
    }

    public static boolean isValidBookShelf(Level level, BlockPos blockPos, BlockPos blockPosOffset) {
        BlockPos targetPos = blockPos.offset(blockPosOffset);
        if (EnchantingBehavior.get().getEnchantmentPower(level.getBlockState(targetPos), level, targetPos) != 0.0f) {
            BlockPos inBetweenPos = blockPos.offset(blockPosOffset.getX() / 2, blockPosOffset.getY(), blockPosOffset.getZ() / 2);
            BlockState blockState = level.getBlockState(inBetweenPos);
            return !blockState.isSolidRender() || blockState.is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
        }
        return false;
    }

    public InfuserType getType() {
        return this.type;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfuserBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? createTickerHelper(blockEntityType, ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, InfuserBlockEntity::tick) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof InfuserBlockEntity infuser) {
            if (!level.isClientSide()) {
                player.openMenu(infuser);
                player.containerMenu.slotsChanged(infuser);
                if (this.type == InfuserType.ADVANCED) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.4f);
                } else {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.0f);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0 || !isValidBookShelf(level, pos, offset)) continue;
            level.addParticle(
                    ParticleTypes.ENCHANT,
                    (double) pos.getX() + 0.5,
                    (double) pos.getY() + 2.0,
                    (double) pos.getZ() + 0.5,
                    (double) ((float) offset.getX() + random.nextFloat()) - 0.5,
                    (double) ((float) offset.getY() - random.nextFloat() - 1.0f),
                    (double) ((float) offset.getZ() + random.nextFloat()) - 0.5
            );
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean isMoving) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof Container container) {
            Containers.dropContents(serverLevel, blockPos, container);
        }
        super.affectNeighborsAfterRemoval(blockState, serverLevel, blockPos, isMoving);
    }

    public Component getDescriptionComponent() {
        if (this.type == InfuserType.ADVANCED) {
            return Component.translatable("block.advancedenchant.enchanting_infuser.description.choose_and_modify");
        }
        return Component.translatable("block.advancedenchant.enchanting_infuser.description.choose");
    }
}
