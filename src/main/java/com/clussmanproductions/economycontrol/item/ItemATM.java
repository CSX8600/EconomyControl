package com.clussmanproductions.economycontrol.item;

import com.clussmanproductions.economycontrol.blocks.BlockATM;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemATM extends ItemBlock {

	public ItemATM(BlockATM block) {
		super(block);
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up()))
		{
			return false;
		}
		
		return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
	}

}
