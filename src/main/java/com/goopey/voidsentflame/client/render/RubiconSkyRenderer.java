package com.goopey.voidsentflame.client.render;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.client.render.sky.CrucibleSkyRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

public class RubiconSkyRenderer implements ResourceManagerReloadListener, AutoCloseable {
  public static final String NAME = "rubicon_sky";
  public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "shaders/" + NAME + ".reload");
  public static RubiconSkyRenderer INSTANCE = new RubiconSkyRenderer();

  // all the different sky renderers this main SkyRenderer class manages
  public CrucibleSkyRenderer crucibleSky = new CrucibleSkyRenderer();

  private RubiconSkyRenderer() {}

  /**
   * This method takes care of automatically closing all skyRenderer classes this main class manages.
   */
  @Override
  public void close() {
    this.crucibleSky.close();
  }

  /**
   * Used to initialize the resources needed by a skyRenderer when the game reloads assets
   * @param resourceManager Minecraft's resource manager. Provides controlled access to the game's files while running.
   */
  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    this.crucibleSky.onResourceManagerReload(resourceManager);
  }

  //######################################################
  //                  RENDER STUFF
  //######################################################

  /**
   * Main method. Called when attempting to render the sky in any biome in the Rubicon dimension.
   * @param event the event bus event. Needed to obtain poseStack, matrixStack and other critical objects.
   */
  public void render(RenderLevelStageEvent.AfterSky event) {
    // Check if in proper biomes
  }
}
