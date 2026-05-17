package com.goopey.voidsentflame.block.blockentity;

import com.goopey.voidsentflame.core.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidSeaLayerBlockEntity extends BlockEntity {
   public static final int WIDTH = 16;
   public static final int LENGTH = 16;
   public static final int HEIGHT_OFFSET = 24;

   public VoidSeaLayerBlockEntity(BlockPos pos, BlockState blockState) {
      super(BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get(), pos, blockState);
   }

  //#################################################
  //                 BLOCK ENTITY
  //#################################################

   @Override
   public BlockEntityType<?> getType() {
      return BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get();
   }
}