package com.goopey.voidsentflame;

import com.goopey.voidsentflame.client.VoidSeaUnbakedModelLoader;
import com.goopey.voidsentflame.client.render.VoidSeaLayerBlockEntityRenderer;
import com.goopey.voidsentflame.client.render.VoidSeaRenderer;
import com.goopey.voidsentflame.core.StandaloneModelRegistry;
import com.goopey.voidsentflame.core.init.BlockEntityInit;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

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
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get(), VoidSeaLayerBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterEntities event) {
        VoidSeaRenderer.getInstance().render(event);
    }

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterLoaders event) {
        event.register(VoidSeaUnbakedModelLoader.ID, VoidSeaUnbakedModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        StandaloneModelRegistry.registerStandaloneModels(event);
    }
}
