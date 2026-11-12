package com.oidioomstudio.advancedenchant.world.level.block;

import com.mojang.serialization.Codec;
import com.oidioomstudio.advancedenchant.config.AdvancedEnchantConfig;
import com.oidioomstudio.advancedenchant.init.ModRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;

public enum InfuserType implements StringRepresentable {
    NORMAL {
        @Override
        public TagKey<Enchantment> getAvailableEnchantments() {
            return ModRegistry.IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
        }

        @Override
        public AdvancedEnchantConfig.InfuserConfig getConfig() {
            return AdvancedEnchantConfig.get().normalInfuser;
        }
    },
    ADVANCED {
        @Override
        public TagKey<Enchantment> getAvailableEnchantments() {
            return ModRegistry.IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG;
        }

        @Override
        public AdvancedEnchantConfig.InfuserConfig getConfig() {
            return AdvancedEnchantConfig.get().advancedInfuser;
        }
    };

    public static final Codec<InfuserType> CODEC = StringRepresentable.fromEnum(InfuserType::values);
    public static final StreamCodec<ByteBuf, InfuserType> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            i -> InfuserType.values()[i],
            InfuserType::ordinal
    );

    public abstract TagKey<Enchantment> getAvailableEnchantments();

    public abstract AdvancedEnchantConfig.InfuserConfig getConfig();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
