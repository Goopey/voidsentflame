package com.goopey.voidsentflame.core.init;

import java.util.function.Supplier;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.fluid.VoidFluid;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FluidInit {
  public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(BuiltInRegistries.FLUID, VoidsentFlameMod.MODID);
	public static final Supplier<BaseFlowingFluid> VOID_FLUID = REGISTRY.register("void_fluid", () -> new VoidFluid.Source());
	public static final Supplier<BaseFlowingFluid> FLOWING_VOID_FLUID = REGISTRY.register("flowing_void_fluid", () -> new VoidFluid.Flowing());

	@EventBusSubscriber(Dist.CLIENT)
	public static class FluidsClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(VOID_FLUID.get(), ChunkSectionLayer.TRANSLUCENT);
			ItemBlockRenderTypes.setRenderLayer(FLOWING_VOID_FLUID.get(), ChunkSectionLayer.TRANSLUCENT);
		}
	}
}
