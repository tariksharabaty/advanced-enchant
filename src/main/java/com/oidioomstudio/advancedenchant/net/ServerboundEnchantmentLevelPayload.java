package com.oidioomstudio.advancedenchant.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.IntUnaryOperator;

public record ServerboundEnchantmentLevelPayload(int containerId, Holder<Enchantment> enchantment, Operation operation) implements CustomPacketPayload {
    public static final Type<ServerboundEnchantmentLevelPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("advancedenchant", "serverbound_enchantment_level")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEnchantmentLevelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerboundEnchantmentLevelPayload::containerId,
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            ServerboundEnchantmentLevelPayload::enchantment,
            Operation.STREAM_CODEC,
            ServerboundEnchantmentLevelPayload::operation,
            ServerboundEnchantmentLevelPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Operation implements IntUnaryOperator {
        ADD(enchantmentLevel -> enchantmentLevel + 1),
        REMOVE(enchantmentLevel -> enchantmentLevel - 1),
        ADD_ALL(operand -> Integer.MAX_VALUE),
        REMOVE_ALL(operand -> Integer.MIN_VALUE);

        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
                i -> Operation.values()[i],
                Operation::ordinal
        );

        private final IntUnaryOperator operator;

        Operation(IntUnaryOperator operator) {
            this.operator = operator;
        }

        @Override
        public int applyAsInt(int enchantmentLevel) {
            return this.operator.applyAsInt(enchantmentLevel);
        }

        public static Operation remove(boolean isShiftKeyDown) {
            return isShiftKeyDown ? REMOVE_ALL : REMOVE;
        }

        public static Operation add(boolean isShiftKeyDown) {
            return isShiftKeyDown ? ADD_ALL : ADD;
        }
    }
}
