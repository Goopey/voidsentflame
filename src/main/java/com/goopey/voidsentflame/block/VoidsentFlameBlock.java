package com.goopey.voidsentflame.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VoidsentFlameBlock extends BaseEntityBlock {
  public static final MapCodec<VoidsentFlameBlock> CODEC = simpleCodec(VoidsentFlameBlock::new);
  private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

  public VoidsentFlameBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }


  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return null;
  }

  //#############################################
  //              CRAFTING TABLE
  //#############################################

  /**
   * Copied over from crafting table code.
   * @param p_52233_ //TODO
   * @param p_52234_ //TODO
   * @param p_52235_ //TODO
   * @param p_52236_ //TODO
   * @param p_52238_ //TODO
   * @return //TODO
   */
  protected InteractionResult useWithoutItem(BlockState p_52233_, Level p_52234_, BlockPos p_52235_, Player p_52236_, BlockHitResult p_52238_) {
    if (!p_52234_.isClientSide()) {
      p_52236_.openMenu(p_52233_.getMenuProvider(p_52234_, p_52235_));
      p_52236_.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    }

    return InteractionResult.SUCCESS;
  }

  /**
   * Copied over from crafting table code.
   * @param state //TODO
   * @param level //TODO
   * @param pos //TODO
   * @return //TODO
   */
  protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
    return new SimpleMenuProvider((containerId, inventory, player) -> new CraftingMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
  }
}
