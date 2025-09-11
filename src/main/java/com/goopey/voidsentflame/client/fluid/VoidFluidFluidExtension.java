package com.goopey.voidsentflame.client.fluid;

import org.joml.Vector4f;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;

import com.goopey.voidsentflame.core.init.FluidTypesInit;

import javax.annotation.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class VoidFluidFluidExtension {
	@SubscribeEvent
	public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent event) {
		event.registerFluidType(new IClientFluidTypeExtensions() {
			private static final ResourceLocation STILL_TEXTURE = ResourceLocation.parse("voidsentflame:block/void_fluid");
			private static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.parse("voidsentflame:block/void_side_fluid");
			private static final ResourceLocation RENDER_OVERLAY_TEXTURE = ResourceLocation.parse("voidsentflame:textures/void_fluid_overlay.png");

			@Override
			public ResourceLocation getStillTexture() {
				return STILL_TEXTURE;
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return FLOWING_TEXTURE;
			}

			@Override
			public ResourceLocation getRenderOverlayTexture(Minecraft minecraft) {
				return RENDER_OVERLAY_TEXTURE;
			}

			@Override
			public Vector4f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
				return new Vector4f(0.937254902f, 0.7490196078f, 0.0156862745f, fluidFogColor.w);
			}

			@Override
			public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
				float nearDistance = fogData.environmentalStart;
				float farDistance = fogData.environmentalEnd;
				Entity entity = camera.getEntity();
				Level world = entity.level();
				fogData.environmentalStart = 0.05f;
				fogData.environmentalEnd = 0.2f;
			}
		}, FluidTypesInit.VOID_FLUID_TYPE.get());
	}
}