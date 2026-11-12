package com.oidioomstudio.advancedenchant.client;

import com.oidioomstudio.advancedenchant.client.gui.screens.inventory.InfuserScreen;
import com.oidioomstudio.advancedenchant.client.renderer.blockentity.InfuserRenderer;
import com.oidioomstudio.advancedenchant.config.AdvancedEnchantConfig;
import com.oidioomstudio.advancedenchant.init.ModRegistry;
import com.oidioomstudio.advancedenchant.net.ClientboundInfuserEnchantmentsPayload;
import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import com.oidioomstudio.olib.api.OConfigCategory;
import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.api.OModConfig;
import com.oidioomstudio.olib.api.options.BooleanOption;
import com.oidioomstudio.olib.api.options.IntegerSliderOption;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AdvancedEnchantClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register Menu Screens
        MenuScreens.register(ModRegistry.INFUSING_MENU_TYPE, InfuserScreen::new);

        // Register Block Entity Renderers
        BlockEntityRenderers.register(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, InfuserRenderer::new);

        // Register Client Network Payload Receiver
        ClientPlayNetworking.registerGlobalReceiver(ClientboundInfuserEnchantmentsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player != null) {
                    AbstractContainerMenu containerMenu = context.client().player.containerMenu;
                    if (containerMenu instanceof InfuserMenu menu && menu.containerId == payload.containerId()) {
                        menu.setInitialEnchantments(context.client().level, payload.itemEnchantments());
                        Screen screen = context.client().gui.screen();
                        if (screen instanceof InfuserScreen infuserScreen) {
                            infuserScreen.refreshSearchResults();
                        }
                    }
                }
            });
        });

        // Register O Lib Config Screen
        registerOLibConfig();
    }

    private static void registerOLibConfig() {
        AdvancedEnchantConfig.Data cfg = AdvancedEnchantConfig.get();

        OConfigRegistry.register(
                OModConfig.builder("advancedenchant", Component.translatable("advancedenchant.name"), "1.1.2")
                        .category(OConfigCategory.builder("general", Component.translatable("advancedenchant.config.general"))
                                .group("incompatibility", Component.translatable("advancedenchant.config.incompatibility"), g -> g
                                        .add(BooleanOption.builder("remove_incompatibility", Component.translatable("advancedenchant.config.remove_incompatibility"))
                                                .description(Component.translatable("advancedenchant.config.remove_incompatibility.desc"))
                                                .defaultValue(cfg.removeEnchantmentIncompatibility)
                                                .getter(() -> cfg.removeEnchantmentIncompatibility)
                                                .setter(v -> {
                                                    cfg.removeEnchantmentIncompatibility = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                        .add(BooleanOption.builder("max_protection_5", Component.translatable("advancedenchant.config.max_protection_5"))
                                                .description(Component.translatable("advancedenchant.config.max_protection_5.desc"))
                                                .defaultValue(cfg.maxProtectionLevel5)
                                                .getter(() -> cfg.maxProtectionLevel5)
                                                .setter(v -> {
                                                    cfg.maxProtectionLevel5 = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                )
                                .group("normal_infuser", Component.translatable("advancedenchant.config.normal_infuser"), g -> g
                                        .add(IntegerSliderOption.builder("normal_bookshelves", Component.translatable("advancedenchant.config.normal_bookshelves"))
                                                .range(0, 64, 1)
                                                .defaultValue(cfg.normalInfuser.maximumBookshelves)
                                                .getter(() -> cfg.normalInfuser.maximumBookshelves)
                                                .setter(v -> {
                                                    cfg.normalInfuser.maximumBookshelves = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                        .add(BooleanOption.builder("normal_allow_books", Component.translatable("advancedenchant.config.normal_allow_books"))
                                                .defaultValue(cfg.normalInfuser.allowBooks)
                                                .getter(() -> cfg.normalInfuser.allowBooks)
                                                .setter(v -> {
                                                    cfg.normalInfuser.allowBooks = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                )
                                .group("advanced_infuser", Component.translatable("advancedenchant.config.advanced_infuser"), g -> g
                                        .add(IntegerSliderOption.builder("advanced_bookshelves", Component.translatable("advancedenchant.config.advanced_bookshelves"))
                                                .range(0, 128, 1)
                                                .defaultValue(cfg.advancedInfuser.maximumBookshelves)
                                                .getter(() -> cfg.advancedInfuser.maximumBookshelves)
                                                .setter(v -> {
                                                    cfg.advancedInfuser.maximumBookshelves = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                        .add(BooleanOption.builder("advanced_allow_books", Component.translatable("advancedenchant.config.advanced_allow_books"))
                                                .defaultValue(cfg.advancedInfuser.allowBooks)
                                                .getter(() -> cfg.advancedInfuser.allowBooks)
                                                .setter(v -> {
                                                    cfg.advancedInfuser.allowBooks = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                        .add(BooleanOption.builder("advanced_anvil_enchants", Component.translatable("advancedenchant.config.advanced_anvil_enchants"))
                                                .description(Component.translatable("advancedenchant.config.advanced_anvil_enchants.desc"))
                                                .defaultValue(cfg.advancedInfuser.allowAnvilEnchantments)
                                                .getter(() -> cfg.advancedInfuser.allowAnvilEnchantments)
                                                .setter(v -> {
                                                    cfg.advancedInfuser.allowAnvilEnchantments = v;
                                                    AdvancedEnchantConfig.save();
                                                })
                                                .build())
                                )
                                .build()
                        )
                        .build()
        );
    }
}
