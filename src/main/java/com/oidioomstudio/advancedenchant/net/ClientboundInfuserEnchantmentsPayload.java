package com.oidioomstudio.advancedenchant.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

public record ClientboundInfuserEnchantmentsPayload(int containerId, Optional<ItemEnchantments> itemEnchantments) implements CustomPacketPayload {
    public static final Type<ClientboundInfuserEnchantmentsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("advancedenchant", "clientbound_infuser_enchantments")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundInfuserEnchantmentsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundInfuserEnchantmentsPayload::containerId,
            ItemEnchantments.STREAM_CODEC.apply(ByteBufCodecs::optional),
            ClientboundInfuserEnchantmentsPayload::itemEnchantments,
            ClientboundInfuserEnchantmentsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
