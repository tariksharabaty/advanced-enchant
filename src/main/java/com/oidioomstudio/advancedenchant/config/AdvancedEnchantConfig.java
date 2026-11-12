package com.oidioomstudio.advancedenchant.config;

import com.oidioomstudio.olib.config.OConfigHolder;

public class AdvancedEnchantConfig {

    public static class CostsConfig {
        public int maximumCost = 30;
        public boolean scaleCostsByVanillaOnly = true;
    }

    public static class RepairConfig {
        public double repairPercentageStep = 0.25;
        public double repairStepMultiplier = 2.0;
    }

    public static class InfuserConfig {
        public int minimumBookshelves = 0;
        public int maximumBookshelves = 15;
        public ModifiableItems allowModifyingEnchantments = ModifiableItems.UNENCHANTED;
        public boolean allowBooks = false;
        public AllowedRepairItems allowRepairing = AllowedRepairItems.NOTHING;
        public boolean increaseAnvilRepairCost = false;
        public boolean allowAnvilEnchantments = false;
        public CostsConfig costs = new CostsConfig();
        public RepairConfig repair = new RepairConfig();
    }

    public static class Data {
        // Universal Enchantment Incompatibility Removal (Feature from Mod 1)
        public boolean removeEnchantmentIncompatibility = true;

        // Max Protection Level 5 setting (Default: false)
        public boolean maxProtectionLevel5 = false;

        // Normal Infuser (Feature from Mod 2)
        public InfuserConfig normalInfuser = new InfuserConfig();

        // Advanced Infuser (Feature from Mod 2)
        public InfuserConfig advancedInfuser = new InfuserConfig();

        public Data() {
            advancedInfuser.minimumBookshelves = 20;
            advancedInfuser.maximumBookshelves = 32;
            advancedInfuser.allowRepairing = AllowedRepairItems.TOOLS_AND_ARMOR;
            advancedInfuser.allowBooks = true;
            advancedInfuser.allowModifyingEnchantments = ModifiableItems.ALL;
            advancedInfuser.allowAnvilEnchantments = true;
        }
    }

    private static final OConfigHolder<Data> HOLDER = new OConfigHolder<>(
            "advancedenchant",
            Data.class,
            Data::new
    );

    private static Data current = new Data();

    public static void load() {
        current = HOLDER.load();
    }

    public static void save() {
        HOLDER.save();
    }

    public static Data get() {
        if (current == null) {
            load();
        }
        return current;
    }
}
