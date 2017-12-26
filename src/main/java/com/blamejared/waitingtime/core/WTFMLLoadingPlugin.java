package com.blamejared.waitingtime.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * To run this in dev you need to add the following to the *VM* Options in the run config
 * <p>
 * -Dfml.coreMods.load=com.blamejared.waitingtime.core.WTFMLLoadingPlugin
 */
@IFMLLoadingPlugin.Name("WTPlugin")
public class WTFMLLoadingPlugin implements IFMLLoadingPlugin {
    
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.blamejared.waitingtime.core.WTClassTransformer"};
    }
    
    @Override
    public String getModContainerClass() {
        return null;
    }
    
    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }
    
    @Override
    public void injectData(Map<String, Object> data) {
    
    }
    
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}