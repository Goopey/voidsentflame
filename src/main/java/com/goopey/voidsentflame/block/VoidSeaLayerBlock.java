package com.goopey.voidsentflame.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Renderer;

import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
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
import net.minecraft.world.level.block.RenderShape;

public class VoidSeaLayerBlock extends BaseEntityBlock {
	public static final MapCodec<VoidSeaLayerBlock> CODEC = simpleCodec(VoidSeaLayerBlock::new);  

	public VoidSeaLayerBlock(BlockBehaviour.Properties properties) {
		super(properties.mapColor(MapColor.COLOR_YELLOW).strength(1000000000f).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true).lightLevel(s -> 15)
			.noCollission().noLootTable().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).destroyTime(Block.INDESTRUCTIBLE).explosionResistance(1000000));
	}

	@Override
	public int getLightBlock(@Nonnull BlockState state) {
		return 15;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		return new VoidSeaLayerBlockEntity(pos, state);
	}
}