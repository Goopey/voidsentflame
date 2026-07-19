package com.goopey.voidsentflame;

import com.goopey.voidsentflame.block.blockentity.render.VoidsentFlameBlockEntityRenderer;
import com.goopey.voidsentflame.client.render.VoidSeaRenderer;

import com.goopey.voidsentflame.client.render.RubiconSkyRenderer;
import com.goopey.voidsentflame.core.VFRenderPipelines;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
import com.goopey.voidsentflame.server.VoidSeaEvent;
import com.goopey.voidsentflame.util.VFRenderConsts;
import com.goopey.voidsentflame.util.VertexMeshHelper;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

import java.util.OptionalDouble;
import java.util.OptionalInt;

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
    public static void frameGraphSetupEvent(FrameGraphSetupEvent event) {
      GpuTextureView tex0 = event.getTargetBundle().main.get().getDepthTextureView();
      if (tex0 == null) {
        VoidsentFlameMod.LOGGER.info("FrameGraphSetup test returned early 1.");
        return;
      }
      if (!RenderSystem.isOnRenderThread()) {
        VoidsentFlameMod.LOGGER.info("FrameGraphSetup test returned early 2.");
        return;
      }

      RubiconSkyRenderer.INSTANCE.importTex = tex0;

//      Tuple<Integer, GpuBuffer> bufferTuple = VertexMeshHelper.buildScreen(VFRenderConsts.RUBICON_PACKED_LIGHT, VFRenderConsts.RUBICON_PACKED_OVERLAY);
//      int screenIndex = bufferTuple.getA();
//      GpuBuffer screenBuffer = bufferTuple.getB();
//
//      GpuTextureView colorTextureViewT = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
//      GpuTextureView depthTextureViewT = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
//      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
//
//      try (RenderPass renderPass = encoder.createRenderPass(
//        () -> "DrawDepth", colorTextureViewT, OptionalInt.empty(), depthTextureViewT, OptionalDouble.empty())
//      ) {
//        renderPass.setPipeline(VFRenderPipelines.BLIT_PIPELINE);
//        renderPass.bindSampler("SamplerIn", tex0);
//        renderPass.setVertexBuffer(0, screenBuffer);
//        renderPass.setIndexBuffer(screenBuffer, VertexFormat.IndexType.SHORT);
//        renderPass.draw(0, screenIndex);
//      }
    }
 
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterParticles event) {
      // Rubicon dimension effects
      // TODO : reenable VoidSeaRender.
      // TODO : improve performance
      VoidSeaRenderer.getInstance().render(event);
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent.AfterOpaqueBlocks event) {
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
