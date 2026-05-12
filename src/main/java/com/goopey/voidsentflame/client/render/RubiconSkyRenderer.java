package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.sky.CrucibleSkyRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class RubiconSkyRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_sky";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");

  public static RubiconSkyRenderer INSTANCE = new RubiconSkyRenderer();
  // all the different sky renderers this main SkyRenderer class manages
  public CrucibleSkyRenderer crucibleSky = new CrucibleSkyRenderer();

  private RubiconSkyRenderer() {}

  @Override
  public void close() {
    this.crucibleSky.close();
  }

  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    this.crucibleSky.onResourceManagerReload(resourceManager);
  }
}
