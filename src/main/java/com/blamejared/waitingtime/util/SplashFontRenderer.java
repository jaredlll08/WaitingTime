package com.blamejared.waitingtime.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SplashFontRenderer extends FontRenderer {
    
    public static final ResourceLocation fontLoc = new ResourceLocation("textures/font/ascii.png");
    public static Texture texture = new Texture(fontLoc, null);
    
    public SplashFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, texture.getLocation(), null, false);
        super.onResourceManagerReload(null);
    }
    
    @Override
    protected void bindTexture(@Nonnull ResourceLocation location) {
        if(location != locationFontTexture)
            throw new IllegalArgumentException();
        texture.bind();
    }
    
    @Nonnull
    @Override
    protected IResource getResource(@Nonnull ResourceLocation location) throws IOException {
        DefaultResourcePack pack = Minecraft.getMinecraft().mcDefaultResourcePack;
        return new SimpleResource(pack.getPackName(), location, pack.getInputStream(location), null, null);
    }
}