package com.oidioomstudio.advancedenchant.mixin;

import com.oidioomstudio.advancedenchant.config.AdvancedEnchantConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void advancedenchant$alwaysCompatible(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
        if (AdvancedEnchantConfig.get().removeEnchantmentIncompatibility) {
            cir.setReturnValue(!first.equals(second));
        }
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void advancedenchant$maxProtectionLevel(CallbackInfoReturnable<Integer> cir) {
        if (AdvancedEnchantConfig.get().maxProtectionLevel5) {
            Enchantment self = (Enchantment) (Object) this;
            if (self.description().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
                if ("enchantment.minecraft.protection".equals(tc.getKey())) {
                    cir.setReturnValue(5);
                }
            }
        }
    }
}
