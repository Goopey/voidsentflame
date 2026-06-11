package com.goopey.voidsentflame.block.blockentity;

import com.goopey.voidsentflame.core.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VoidsentFlameBlockEntity extends BlockEntity {
  private int flameLevel = 0;

  public VoidsentFlameBlockEntity(BlockPos pos, BlockState blockState) {
    super(BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), pos, blockState);
  }

  //#################################################
  //                 BLOCK ENTITY
  //#################################################

  @Override
  public BlockEntityType<?> getType() {
    return BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get();
  }

  /**
   * Loads saved data when the block loads in or smth akin
   * @param input the ValueInput needed to save data
   */
  @Override
  public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.flameLevel = input.getIntOr("flameLevel", 0);
  }

  /**
   * Saves data when the block unloads or smth akin
   * @param output the ValueOutput needed to save data
   */
  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.putInt("flameLevel", this.flameLevel);
  }

  /**
   *
   * @param level
   * @param pos
   * @param state
   * @param blockEntity
   */
  public static void tick(Level level, BlockPos pos, BlockState state, VoidsentFlameBlockEntity blockEntity) {

  }
}
