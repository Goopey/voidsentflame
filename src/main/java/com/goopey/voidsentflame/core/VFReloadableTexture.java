package com.goopey.voidsentflame.core;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class VFReloadableTexture extends ReloadableTexture {
  public VFReloadableTexture(ResourceLocation resourceLocation) {
    super(resourceLocation);
  }

  @Override
  public TextureContents loadContents(@Nonnull ResourceManager resourceManager) throws IOException {
    return TextureContents.load(resourceManager, this.resourceId());
  }
}
