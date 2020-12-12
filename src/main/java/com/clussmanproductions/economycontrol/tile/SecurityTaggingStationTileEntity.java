package com.clussmanproductions.economycontrol.tile;

import java.util.UUID;

import com.clussmanproductions.economycontrol.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SecurityTaggingStationTileEntity extends TileEntity {
	
	private ItemStackHandler itemStackHandler = new ItemStackHandler(3)
	{
		protected void onContentsChanged(int slot) 
		{
			SecurityTaggingStationTileEntity.this.markDirty();
		};
		
		public boolean isItemValid(int slot, ItemStack stack)
		{
			switch(slot)
			{
				case 0:
					return true;
				case 1:
					return stack.getItem() == ModItems.security_tag;
				case 2:
					return false;
				default:
					return true;
			}
		}
	};
	
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (compound.hasKey("items"))
		{
			itemStackHandler.deserializeNBT((NBTTagCompound)compound.getTag("items"));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("items", itemStackHandler.serializeNBT());
		
		return compound;
	}
	
	public boolean canInteractWith(EntityPlayer playerIn)
	{
		return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5, 0.5, 0.5)) <= 64D;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}
		
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
		}
		return super.getCapability(capability, facing);
	}
}
