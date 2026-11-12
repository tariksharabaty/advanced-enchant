package com.oidioomstudio.advancedenchant;

import com.oidioomstudio.advancedenchant.config.AdvancedEnchantConfig;
import com.oidioomstudio.advancedenchant.init.ModRegistry;
import com.oidioomstudio.advancedenchant.net.ClientboundInfuserEnchantmentsPayload;
import com.oidioomstudio.advancedenchant.net.ServerboundEnchantmentLevelPayload;
import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedEnchantMod implements ModInitializer {
    public static final String MOD_ID = "advancedenchant";
    public static final String MOD_NAME = "Advanced Enchant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Advanced Enchant] Initializing Advanced Enchant by Tarek Sharabaty (Oidioom)...");
        AdvancedEnchantConfig.load();

        // Register Network Payloads
        PayloadTypeRegistry.serverboundPlay().register(ServerboundEnchantmentLevelPayload.TYPE, ServerboundEnchantmentLevelPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundInfuserEnchantmentsPayload.TYPE, ClientboundInfuserEnchantmentsPayload.STREAM_CODEC);

        // Server Network Receiver
        ServerPlayNetworking.registerGlobalReceiver(ServerboundEnchantmentLevelPayload.TYPE, (payload, context) -> {
            context.player().level().getServer().execute(() -> {
                AbstractContainerMenu menu = context.player().containerMenu;
                if (menu instanceof InfuserMenu infuserMenu && infuserMenu.containerId == payload.containerId()) {
                    infuserMenu.clickEnchantmentLevelButton(payload.enchantment(), payload.operation());
                }
            });
        });

        // Trigger registry loading
        ModRegistry.bootstrap();
        com.oidioomstudio.olib.api.OCreativeTabs.accept(ModRegistry.ENCHANTING_INFUSER_ITEM);
        com.oidioomstudio.olib.api.OCreativeTabs.accept(ModRegistry.ADVANCED_ENCHANTING_INFUSER_ITEM);
    }
}
