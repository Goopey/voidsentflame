package com.goopey.voidsentflame.client;

import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;
import com.goopey.voidsentflame.client.model.VoidSeaLayerBlockGeoModel;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidSeaLayerBlockEntityRenderer extends GeoBlockRenderer<VoidSeaLayerBlockEntity> {
  public VoidSeaLayerBlockEntityRenderer(GeoModel<VoidSeaLayerBlockEntity> model) {
    super(model);
  }

  public VoidSeaLayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    super(new VoidSeaLayerBlockGeoModel());
  }
}
