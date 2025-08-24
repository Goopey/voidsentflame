package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class ItemInit {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidsentFlameMod.MODID);

  public static final DeferredItem<Item> RUNIC_FRUIT_ITEM = ITEMS.registerItem("runic_fruit_item", iProperties -> new Item(iProperties), new Item.Properties());

  public static final DeferredItem<Item> RUBICON_IGNITER_ITEM = ITEMS.registerItem("rubicon_igniter_item", iProperties -> new Item(iProperties), new Item.Properties());
}