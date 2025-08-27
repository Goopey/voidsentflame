package com.goopey.voidsentflame.block;

import com.goopey.voidsentflame.core.init.FluidInit;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LiquidBlock;

public class VoidFluidBlock extends LiquidBlock {
	public VoidFluidBlock(BlockBehaviour.Properties properties) {
		super(FluidInit.VOID_FLUID.get(), properties.mapColor(MapColor.COLOR_YELLOW).strength(1000000000f).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true).lightLevel(s -> 15).noCollission().noLootTable()
				.liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}

	@Override
	public int getLightBlock(BlockState state) {
		return 15;
	}
}