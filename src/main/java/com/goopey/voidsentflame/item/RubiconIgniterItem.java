package com.goopey.voidsentflame.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;

import com.goopey.voidsentflame.block.RubiconPortalBlock;

import javax.annotation.Nonnull;

public class RubiconIgniterItem extends Item {
	public RubiconIgniterItem(Item.Properties properties) {
		super(properties

				.durability(64));
	}

	@Override
	public InteractionResult useOn(@Nonnull UseOnContext context) {
		Player entity = context.getPlayer();
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		ItemStack itemstack = context.getItemInHand();
		Level world = context.getLevel();
		if (!entity.mayUseItemAt(pos, context.getClickedFace(), itemstack)) {
			return InteractionResult.FAIL;
		} else {
			boolean success = false;
			if (world.isEmptyBlock(pos) && true) {
				RubiconPortalBlock.portalSpawn(world, pos);
				success = true;
			}
			return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
	}
}