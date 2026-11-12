package com.oidioomstudio.advancedenchant.client;

import com.oidioomstudio.advancedenchant.AdvancedEnchantMod;
import com.oidioomstudio.olib.client.OModMenuApi;

public class ModMenuIntegration extends OModMenuApi {
    @Override
    protected String getModId() {
        return AdvancedEnchantMod.MOD_ID;
    }
}

