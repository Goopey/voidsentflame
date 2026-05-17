package com.goopey.voidsentflame.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;

public class VoidSeaLayerBlock extends BaseEntityBlock {
	public static final MapCodec<VoidSeaLayerBlock> CODEC = simpleCodec(VoidSeaLayerBlock::new);  

	public VoidSeaLayerBlock(BlockBehaviour.Properties properties) {
		super(properties.mapColor(MapColor.COLOR_YELLOW).strength(1000000000f).hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true).lightLevel(s -> 15)
			.noCollision().noLootTable().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).destroyTime(Block.INDESTRUCTIBLE).explosionResistance(1000000));
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

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return null;
	}
}