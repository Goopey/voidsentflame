package com.goopey.voidsentflame.core;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;

public class VFTextureManager extends TextureManager {
  //Singleton
  private static VFTextureManager INSTANCE;

  //TODO : remove if unneeded

  private VFTextureManager(ResourceManager resourceManager) {
    super(resourceManager);
  }

  public static VFTextureManager initInstance(ResourceManager resourceManager) {
    INSTANCE = new VFTextureManager(resourceManager);
    return INSTANCE;
  }

  public static VFTextureManager getInstance() {
    return INSTANCE;
  }
}
