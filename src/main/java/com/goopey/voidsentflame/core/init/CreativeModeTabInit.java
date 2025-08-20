package com.goopey.voidsentflame.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.goopey.voidsentflame.VoidsentFlameMod;

public class CreativeModeTabInit {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
    DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidsentFlameMod.MODID);

  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> VOIDSENTFLAME_ITEMS_TAB = CREATIVE_MODE_TAB.register("voidsentflame_items_tab",
    () -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemInit.RUNIC_FRUIT.get()))
      .title(Component.translatable("creativetab.voidsentflame.voidsentflame_items"))
      .displayItems((itemDisplayParameters, output) -> {
        output.accept(ItemInit.RUNIC_FRUIT);
        output.accept(ItemInit.RUBICON_IGNITER);
      }).build());

  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> VOIDSENTFLAME_BLOCK_TAB = CREATIVE_MODE_TAB.register("voidsentflame_blocks_tab",
    () -> CreativeModeTab.builder().icon(() -> new ItemStack(BlockInit.VOID_STONE.get()))
      .withTabsBefore(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "voidsentflame_items_tab"))
      .title(Component.translatable("creativetab.voidsentflame.voidsentflame_blocks"))
      .displayItems((itemDisplayParameters, output) -> {
        output.accept(BlockInit.VOID_STONE.get());
      }).build());
}