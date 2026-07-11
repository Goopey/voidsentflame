package com.goopey.voidsentflame.world.dimension;

import javax.annotation.Nonnull;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class RubiconDimension {
  public static final ResourceKey<Level> RUBICON = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("voidsentflame:rubicon"));

  @EventBusSubscriber(Dist.CLIENT)
	public static class RubiconSpecialEffectsHandler {
		@SubscribeEvent
		public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
			DimensionSpecialEffects customEffect = new DimensionSpecialEffects(DimensionSpecialEffects.SkyType.OVERWORLD, false, false) {
				@Override
				public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
					return new Vec3(0.2, 0, 0.1058823529);
				}

				@Override
				public boolean isFoggyAt(int x, int y) {
					return false;
				}
			};
			event.register(ResourceLocation.parse("voidsentflame:rubicon"), customEffect);
		}
	}

  public static class VoidSeaConstants {
    // Render Waves
    public static final float WAVE_AMPLITUDE = 1.5f;
    public static final float WAVE_FREQUENCY = 0.15f;
    public static final float TIME_FREQUENCY = 4.f;
    public static final float SECONDS_PER_DAY = 1200.f;

    // World Position
    public static final double HEIGHT = -42.5;
    public static final double HEAT_HEIGHT = -26.5;
    public static final int OFFSET = 256;
    public static final int VIEW_DISTANCE_SCALE = 12;
  }
}
