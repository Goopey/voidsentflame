package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.item.RubiconIgniterItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidsentFlameMod.MODID);

  public static final DeferredItem<Item> RUNIC_FRUIT_ITEM = ITEMS.registerItem("runic_fruit_item", iProperties -> new Item(iProperties), new Item.Properties());

  public static final DeferredItem<RubiconIgniterItem> RUBICON_IGNITER_ITEM = ITEMS.registerItem("rubicon_igniter_item", iProperties -> new RubiconIgniterItem(iProperties), new Item.Properties().stacksTo(1));
}