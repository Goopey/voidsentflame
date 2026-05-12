package com.goopey.voidsentflame;

import com.goopey.voidsentflame.client.render.VoidSeaRenderer;

import com.goopey.voidsentflame.client.render.VoidsentFlameSkyRenderer;
import com.goopey.voidsentflame.server.VoidSeaEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
      VoidsentFlameMod.LOGGER.info("HELLO FROM CLIENT SETUP");
      VoidsentFlameMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
      VoidSeaEvent.voidSeaTick(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
//      event.registerBlockEntityRenderer(BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get(), VoidSeaLayerBlockEntityRenderer::new);
    }
 
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
      // Rubicon dimension effects
      VoidSeaRenderer.getInstance().render(event);
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent.AfterSky event) {

    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        // TODO : fix no registering when not on renderThread
      // VoidsentFlameMod.LOGGER.info("###################################" + event.getLastVanillaListener().getClass().toGenericString());
      // VoidsentFlameMod.LOGGER.info("###################################" + event.getRegistry());
//       TODO : remove test code (examples for addListeners)
//      CloudRenderer e;
//      GameRenderer e5;
//      SkyRenderer e;
//      Minecraft e;
//      LevelRenderer e;
//      PeriodicNotificationManager e2;
//      FoliageColorReloadListener e3;
//      ClientHooks e4;
      event.addListener(VoidsentFlameSkyRenderer.LOCATION, VoidsentFlameSkyRenderer.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientStopping(ClientStoppingEvent event) {
      VoidsentFlameSkyRenderer.INSTANCE.close();
    }

    @SubscribeEvent
    public static void registerRenderPipelinesEvent(RegisterRenderPipelinesEvent event) {
        // TODO : move registering custom RenderPipelines to proper event
    }

    @SubscribeEvent
    public static void registerRenderPipelinesEvent(RegisterRenderBuffersEvent event) {
        // TODO : move registering custom RenderBuffers to proper event
    }
}
