package com.goopey.voidsentflame.block;

import javax.annotation.Nullable;

import com.goopey.voidsentflame.block.blockentity.VoidsentBonfireBlockEntity;
import com.goopey.voidsentflame.core.init.FluidInit;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;

public class VoidsentBonfireBlock extends BaseEntityBlock {
	public static final MapCodec<VoidsentBonfireBlock> CODEC = simpleCodec(VoidsentBonfireBlock::new);  

	public VoidsentBonfireBlock(BlockBehaviour.Properties properties) {
		super(properties.mapColor(MapColor.COLOR_YELLOW).strength(1000000000f).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true).lightLevel(s -> 15)
			.noCollission().noLootTable().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).destroyTime(Block.INDESTRUCTIBLE).explosionResistance(1000000));
	}

	@Override
	public int getLightBlock(BlockState state) {
		return 15;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VoidsentBonfireBlockEntity(pos, state);
	}
}