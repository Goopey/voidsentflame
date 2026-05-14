package com.goopey.voidsentflame;

import com.goopey.voidsentflame.core.init.*;
import org.slf4j.Logger;

import com.goopey.voidsentflame.datagen.DataGenerators;
import com.goopey.voidsentflame.world.features.StructureFeature;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(VoidsentFlameMod.MODID)
public class VoidsentFlameMod {
  // Define mod id in a common place for everything to reference
  public static final String MODID = "voidsentflame";
  // Directly reference a slf4j logger
  public static final Logger LOGGER = LogUtils.getLogger();

  // The constructor for the mod class is the first code that is run when your mod is loaded.
  // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
  public VoidsentFlameMod(IEventBus modEventBus, ModContainer modContainer) {
    // Register the commonSetup method for mod loading
    modEventBus.addListener(this::commonSetup);
    modEventBus.addListener(this::onClientSetup);

    // Register EVENT_BUS
    NeoForge.EVENT_BUS.register(this);

    // register Init
    CreativeModeTabInit.CREATIVE_MODE_TAB.register(modEventBus);
    ItemInit.ITEMS.register(modEventBus);
    BlockInit.BLOCKS.register(modEventBus);
    FluidInit.REGISTRY.register(modEventBus);
    FluidTypesInit.REGISTRY.register(modEventBus);
    StructureFeature.REGISTRY.register(modEventBus);
    BlockEntityInit.BLOCK_ENTITY.register(modEventBus);
    SoundInit.SOUNDS.register(modEventBus);

    // listeners
    modEventBus.addListener(this::addCreative);
    modEventBus.addListener(DataGenerators::gatherDataServer);
    modEventBus.addListener(DataGenerators::gatherDataClient);

    // Register the item to a creative tab
    // modEventBus.addListener(this::addCreative);

    //  Register Config
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
    // To add items to vanilla creative tabs
  }

  private void commonSetup(FMLCommonSetupEvent event) {
    // Some common setup code
    LOGGER.info("HELLO FROM VOIDSENTFLAME COMMON SETUP");
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {
    // Do something when the server starts
    LOGGER.info("HELLO FROM VOIDSENTFLAME SERVER STARTING");
  }

  public void onClientSetup(final FMLClientSetupEvent event) {
    //client setup stuff
  }
}
