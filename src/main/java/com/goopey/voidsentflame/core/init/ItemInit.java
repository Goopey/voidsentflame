package com.goopey.voidsentflame.core.init;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.item.RubiconIgniterItem;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidsentFlameMod.MODID);

  public static final DeferredItem<Item> RUNIC_FRUIT_ITEM = ITEMS.registerItem(
    "runic_fruit_item", Item::new,
    Item.Properties::new
  );

  public static final DeferredItem<RubiconIgniterItem> RUBICON_IGNITER_ITEM = ITEMS.registerItem(
    "rubicon_igniter_item", RubiconIgniterItem::new,
    () -> new Item.Properties().stacksTo(1)
  );

  // Adds in a bucket for VOID FLUID
  public static final DeferredItem<BucketItem> VOID_FLUID_BUCKET = ITEMS.registerItem(
    "void_fluid_bucket", iProperties -> new BucketItem(FluidInit.VOID_FLUID.get(), iProperties),
    () -> new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)
  );
}