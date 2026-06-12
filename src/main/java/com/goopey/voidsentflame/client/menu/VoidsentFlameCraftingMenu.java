package com.goopey.voidsentflame.client.menu;

import com.goopey.voidsentflame.core.init.BlockInit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class VoidsentFlameCraftingMenu extends CraftingMenu {
  private final ContainerLevelAccess access;

  public VoidsentFlameCraftingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
    super(containerId, playerInventory, access);
    this.access = access;
  }

  @Override
  public boolean stillValid(Player player) {
    return stillValid(this.access, player, BlockInit.VOIDSENT_FLAME_BLOCK.get());
  }
}
