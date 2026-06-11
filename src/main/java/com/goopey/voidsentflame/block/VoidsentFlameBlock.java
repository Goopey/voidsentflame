package com.goopey.voidsentflame.block;

import com.goopey.voidsentflame.block.blockentity.VoidsentFlameBlockEntity;
import com.goopey.voidsentflame.core.init.BlockEntityInit;
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
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VoidsentFlameBlock extends BaseEntityBlock {
  public static final MapCodec<VoidsentFlameBlock> CODEC = simpleCodec(VoidsentFlameBlock::new);
  private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

  public VoidsentFlameBlock(Properties properties) {
    super(properties);
  }

  //#############################################
  //              BLOCK ENTITY
  //#############################################

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    // You can return different tickers here, depending on whatever factors you want. A common use case would be
    // to return different tickers on the client or server, only tick one side to begin with,
    // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
    return createTickerHelper(type, BlockEntityInit.VOIDSENT_FLAME_BLOCK_ENTITY.get(), VoidsentFlameBlockEntity::tick);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
    return null;
  }

  //#############################################
  //              CRAFTING TABLE
  //#############################################

  /**
   * Copied over from crafting table code.
   * @param blockState //TODO
   * @param level //TODO
   * @param pos //TODO
   * @param player //TODO
   * @param hitResult //TODO,
   * @return //TODO
   */
  protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!level.isClientSide()) {
      player.openMenu(blockState.getMenuProvider(level, pos));
      player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
      player.awardStat(Stats.INTERACT_WITH_FURNACE);
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
