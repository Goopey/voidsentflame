package com.goopey.voidsentflame.block.blockentity;

import com.goopey.voidsentflame.core.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VoidSeaLayerBlockEntity extends BlockEntity implements GeoBlockEntity {
	public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public VoidSeaLayerBlockEntity(BlockPos pos, BlockState blockState) {
      super(BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get(), pos, blockState);
   }

  //#################################################
  //                 BLOCK ENTITY
  //#################################################

   @Override
   public BlockEntityType<?> getType() {
      return BlockEntityInit.VOID_SEA_LAYER_BLOCK_ENTITY.get();
   }

   //##################################
	//				GECKOLIB STUFF
	//##################################

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(animTest -> {
			return animTest.setAndContinue(DefaultAnimations.IDLE);
		}));
	}

   @Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}