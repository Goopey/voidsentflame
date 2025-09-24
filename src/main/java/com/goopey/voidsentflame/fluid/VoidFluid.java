package com.goopey.voidsentflame.fluid;

import com.goopey.voidsentflame.core.init.FluidInit;
import com.goopey.voidsentflame.core.init.FluidTypesInit;
import com.goopey.voidsentflame.core.init.ItemInit;

import javax.annotation.Nonnull;

import com.goopey.voidsentflame.core.init.BlockInit;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;

public class VoidFluid extends BaseFlowingFluid {
	public static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(() -> FluidTypesInit.VOID_FLUID_TYPE.get(), () -> FluidInit.VOID_FLUID.get(),
		() -> FluidInit.FLOWING_VOID_FLUID.get()).explosionResistance(1000000000f).tickRate(15).slopeFindDistance(1).bucket(() -> ItemInit.VOID_FLUID_BUCKET.get())
		.block(() -> (LiquidBlock) BlockInit.VOID_FLUID_BLOCK.get());
	
  protected VoidFluid(Properties properties) {
    super(PROPERTIES);
  }

	@Override
	public int getAmount(FluidState arg0) {
		throw new UnsupportedOperationException("Use Source or Flowing");
	}

	@Override
	public boolean isSource(FluidState arg0) {
		throw new UnsupportedOperationException("Use Source or Flowing");
	}

  public static class Source extends VoidFluid {
		public Source() {
			super(PROPERTIES);
		}

		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends VoidFluid {
		public Flowing() {
			super(PROPERTIES);
		}

		protected void createFluidStateDefinition(@Nonnull StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
