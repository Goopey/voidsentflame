package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ItemInit {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidsentFlameMod.MODID);

  public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties()));
}