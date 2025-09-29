package com.goopey.voidsentflame.client.model;

import javax.annotation.Nullable;

import com.goopey.voidsentflame.VoidsentFlameMod;
import com.goopey.voidsentflame.block.blockentity.VoidSeaLayerBlockEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class VoidSeaLayerBlockGeoModel extends DefaultedBlockGeoModel<VoidSeaLayerBlockEntity> {
	public VoidSeaLayerBlockGeoModel() {
		super(ResourceLocation.fromNamespaceAndPath(VoidsentFlameMod.MODID, "geckolib/models/void_sea_layer_block"));
	}

	@Nullable
	@Override
	public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
		return RenderType.entityTranslucent(texture);
	}
}