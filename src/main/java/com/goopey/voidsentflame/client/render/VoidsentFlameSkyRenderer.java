package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.sky.CrucibleSkyRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class VoidsentFlameSkyRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_sky";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");

  public static VoidsentFlameSkyRenderer INSTANCE = new VoidsentFlameSkyRenderer();
  public CrucibleSkyRenderer crucibleSky = new CrucibleSkyRenderer();

  private VoidsentFlameSkyRenderer() {}

  @Override
  public void close() {
    this.crucibleSky.close();
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    this.crucibleSky.onResourceManagerReload(resourceManager);
  }
}
