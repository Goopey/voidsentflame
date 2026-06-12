package com.goopey.voidsentflame;

import com.goopey.voidsentflame.block.blockentity.render.VoidsentFlameBlockEntityRenderer;
import com.goopey.voidsentflame.client.render.VoidSeaRenderer;

import com.goopey.voidsentflame.client.render.RubiconSkyRenderer;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
import com.goopey.voidsentflame.server.VoidSeaEvent;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
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

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
      VoidSeaEvent.voidSeaTick(event);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), VoidsentFlameBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
//      FogRenderer e;
    }
 
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
      // Rubicon dimension effects
      // TODO : reenable VoidSeaRender.
      // TODO : improve performance
//      VoidSeaRenderer.getInstance().render(event);
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent.AfterSky event) {
      RubiconSkyRenderer.INSTANCE.render(event);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
      event.addListener(VoidSeaRenderer.LOCATION, VoidSeaRenderer.getInstance());
      event.addListener(RubiconSkyRenderer.LOCATION, RubiconSkyRenderer.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientStopping(ClientStoppingEvent event) {
      VoidSeaRenderer.getInstance().close();
      RubiconSkyRenderer.INSTANCE.close();
    }
}
