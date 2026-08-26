package com.goopey.voidsentflame;

import com.goopey.voidsentflame.block.blockentity.render.VoidsentFlameBlockEntityRenderer;
import com.goopey.voidsentflame.client.render.RubiconFogRenderer;
import com.goopey.voidsentflame.client.render.VFExtractSkyRendererState;
import com.goopey.voidsentflame.client.render.VoidSeaRenderer;

import com.goopey.voidsentflame.client.render.RubiconSkyRenderer;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
import com.goopey.voidsentflame.server.VoidSeaEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = VoidsentFlameMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = VoidsentFlameMod.MODID, value = Dist.CLIENT)
public class VoidsentFlameModClient {
  public VoidsentFlameModClient(ModContainer container) {
    // Allows NeoForge to create a config screen for this mod's configs.
    // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
    // Do not forget to add translations for your config options to the en_us.json file.
    container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
  }

  @SubscribeEvent
  static void onClientSetup(FMLClientSetupEvent event) {
    // Some client setup code
    VoidsentFlameMod.LOGGER.info("HELLO FROM VOIDSENTFLAME");
  }

  //#################################################
  //                REGISTER EVENTS
  //#################################################

  @SubscribeEvent
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), VoidsentFlameBlockEntityRenderer::new);
  }

  //#################################################
  //                  TICK EVENTS
  //#################################################

  @SubscribeEvent
  public static void onEntityTick(EntityTickEvent.Post event) {
    VoidSeaEvent.voidSeaTick(event);
  }

  //#################################################
  //              RENDERING EVENTS
  //#################################################

  // Extracting States
  @SubscribeEvent
  public static void extractRenderStateEvent(ExtractLevelRenderStateEvent event) {
    VFExtractSkyRendererState.VFExtractSkyRenderStateEvent(event);
  }

  // Start of rendering pipeline.
  @SubscribeEvent
  public static void frameGraphSetupEvent(FrameGraphSetupEvent event) {
  }

  // First render event.
  @SubscribeEvent
  public static void onRenderAfterSky(RenderLevelStageEvent.AfterSky event) {
    // Rubicon Dimension effects
    RubiconSkyRenderer.INSTANCE.render(event);
  }

  @SubscribeEvent
  public static void onRenderAfterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
  }

  @SubscribeEvent
  public static void onRenderAfterEntities(RenderLevelStageEvent.AfterEntities event) {
  }

  @SubscribeEvent
  public static void onRenderAfterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
    // Rubicon Dimension Effects
    // TODO : implement custom fog renderer
    RubiconFogRenderer.INSTANCE.render(event);
  }

  @SubscribeEvent
  public static void onRenderAfterTripwires(RenderLevelStageEvent.AfterTripwireBlocks event) {
  }

  @SubscribeEvent
  public static void onRenderAfterParticles(RenderLevelStageEvent.AfterParticles event) {
  }

  @SubscribeEvent
  public static void onRenderAfterWeather(RenderLevelStageEvent.AfterWeather event) {
    // Rubicon dimension effects
    // TODO : improve performance
    VoidSeaRenderer.INSTANCE.render(event);
  }

  // Last rendering event.
  @SubscribeEvent
  public static void onRenderAfterLevel(RenderLevelStageEvent.AfterLevel event) {
  }

  //###########################################
  //            RELOAD LISTENERS
  //###########################################

  // Manages loading/reloading sprites anytime a world is loaded, render distance changes, player reloads packs...
  @SubscribeEvent
  public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
    event.addListener(VoidSeaRenderer.LOCATION, VoidSeaRenderer.INSTANCE);
    event.addListener(RubiconSkyRenderer.LOCATION, RubiconSkyRenderer.INSTANCE);
    event.addListener(RubiconFogRenderer.LOCATION, RubiconFogRenderer.INSTANCE);
  }

  // Manages closing renderer whenever the player closes their game to avoid crashes during closing
  @SubscribeEvent
  public static void onClientStopping(ClientStoppingEvent event) {
    VoidSeaRenderer.INSTANCE.close();
    RubiconSkyRenderer.INSTANCE.close();
    RubiconFogRenderer.INSTANCE.close();
  }
}
