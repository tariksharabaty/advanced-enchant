package com.oidioomstudio.advancedenchant.init;

import com.oidioomstudio.advancedenchant.world.inventory.InfuserMenu;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserBlock;
import com.oidioomstudio.advancedenchant.world.level.block.InfuserType;
import com.oidioomstudio.advancedenchant.world.level.block.entity.InfuserBlockEntity;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModRegistry {
    public static final String MOD_ID = "advancedenchant";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    // Resource Keys
    public static final ResourceKey<Block> ENCHANTING_INFUSER_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id("enchanting_infuser"));
    public static final ResourceKey<Block> ADVANCED_ENCHANTING_INFUSER_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, id("advanced_enchanting_infuser"));
    public static final ResourceKey<Item> ENCHANTING_INFUSER_ITEM_KEY = ResourceKey.create(Registries.ITEM, id("enchanting_infuser"));
    public static final ResourceKey<Item> ADVANCED_ENCHANTING_INFUSER_ITEM_KEY = ResourceKey.create(Registries.ITEM, id("advanced_enchanting_infuser"));

    // Blocks
    public static final Block ENCHANTING_INFUSER = Registry.register(
            BuiltInRegistries.BLOCK,
            ENCHANTING_INFUSER_BLOCK_KEY,
            new InfuserBlock(InfuserType.NORMAL, BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).setId(ENCHANTING_INFUSER_BLOCK_KEY))
    );

    public static final Block ADVANCED_ENCHANTING_INFUSER = Registry.register(
            BuiltInRegistries.BLOCK,
            ADVANCED_ENCHANTING_INFUSER_BLOCK_KEY,
            new InfuserBlock(InfuserType.ADVANCED, BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).setId(ADVANCED_ENCHANTING_INFUSER_BLOCK_KEY))
    );

    // Items
    public static final Item ENCHANTING_INFUSER_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            ENCHANTING_INFUSER_ITEM_KEY,
            new BlockItem(ENCHANTING_INFUSER, new Item.Properties().useBlockDescriptionPrefix().setId(ENCHANTING_INFUSER_ITEM_KEY))
    );

    public static final Item ADVANCED_ENCHANTING_INFUSER_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            ADVANCED_ENCHANTING_INFUSER_ITEM_KEY,
            new BlockItem(ADVANCED_ENCHANTING_INFUSER, new Item.Properties().useBlockDescriptionPrefix().setId(ADVANCED_ENCHANTING_INFUSER_ITEM_KEY))
    );

    // Creative Mode Tab
    public static final ResourceKey<CreativeModeTab> CREATIVE_MODE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            id("main")
    );

    public static final CreativeModeTab CREATIVE_MODE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            CREATIVE_MODE_TAB_KEY,
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.advancedenchant.main"))
                    .icon(() -> new ItemStack(ADVANCED_ENCHANTING_INFUSER_ITEM))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ENCHANTING_INFUSER_ITEM);
                        output.accept(ADVANCED_ENCHANTING_INFUSER_ITEM);
                    })
                    .build()
    );

    // Block Entity Type
    public static final BlockEntityType<InfuserBlockEntity> INFUSER_BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            id("enchanting_infuser"),
            FabricBlockEntityTypeBuilder.create(InfuserBlockEntity::new, ENCHANTING_INFUSER, ADVANCED_ENCHANTING_INFUSER).build()
    );

    // Menu Type
    public static final MenuType<InfuserMenu> INFUSING_MENU_TYPE = Registry.register(
            BuiltInRegistries.MENU,
            id("infusing"),
            new ExtendedMenuType<>(InfuserMenu::new, InfuserType.STREAM_CODEC)
    );

    // Tags
    public static final TagKey<Enchantment> IN_ENCHANTING_INFUSER_ENCHANTMENT_TAG = TagKey.create(
            Registries.ENCHANTMENT,
            id("in_enchanting_infuser")
    );

    public static final TagKey<Enchantment> IN_ADVANCED_ENCHANTING_INFUSER_ENCHANTMENT_TAG = TagKey.create(
            Registries.ENCHANTMENT,
            id("in_advanced_enchanting_infuser")
    );

    public static void bootstrap() {
        // Trigger static init
    }
}
