package com.clussmanproductions.economycontrol.gui;

import java.util.Optional;
import java.util.UUID;

import com.clussmanproductions.economycontrol.ModItems;
import com.clussmanproductions.economycontrol.tile.SecurityTaggingStationTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSecurityTaggingStation extends Container {

	SecurityTaggingStationTileEntity te;
	public UUID company = null;
	public Optional<Long> tagCost;
	public Runnable onItemInserted;
	
	public ContainerSecurityTaggingStation(IInventory playerInventory, SecurityTaggingStationTileEntity te)
	{
		this.te = te;
		
		addOwnSlots();
		addPlayerSlots(playerInventory);
	}
	
	private void addOwnSlots()
	{
		IItemHandler itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		addSlotToContainer(new SlotItemHandler(itemHandler, 0, 30, 35));
		addSlotToContainer(new SlotItemHandler(itemHandler, 1, 66, 35));
		addSlotToContainer(new SlotItemHandler(itemHandler, 2, 124, 35));
	}
	
	private void addPlayerSlots(IInventory playerInventory)
	{
		for(int row = 1; row < 4; row++)
		{
			for(int col = 0; col < 9; col++)
			{
				int x = 8 + col * 18;
				int y = row * 18 + 66;
				this.addSlotToContainer(new Slot(playerInventory, col + row * 9, x, y));
			}
		}
		
		for (int row = 0; row < 9; row++)
		{
			int x = 8 + row * 18;
			int y = 76 + 66;
			this.addSlotToContainer(new Slot(playerInventory, row, x, y));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 3) {
                if (!this.mergeItemStack(itemstack1, 3, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return te.canInteractWith(playerIn);
	}

	public void tryCreateSecurityTaggedItem()
	{
		if (company != null && tagCost != null && getSlot(0).getStack().getItem() != Items.AIR && getSlot(1).getStack().getItem() == ModItems.security_tag)
		{
			ItemStack stack = getSlot(0).getStack();
			
			ItemStack taggedItem = new ItemStack(ModItems.security_tagged_item);
			NBTTagCompound compound = taggedItem.getTagCompound();
			if (compound == null)
			{
				compound = new NBTTagCompound();
			}
			
			compound.setTag("internalItem", stack.serializeNBT());
			compound.setUniqueId("company", company);
			compound.setLong("tagCost", tagCost.get());
			taggedItem.setTagCompound(compound);
			
			getSlot(2).putStack(taggedItem);
		}
		else
		{
			getSlot(2).putStack(new ItemStack(Items.AIR));
		}
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		ItemStack stack = super.slotClick(slotId, dragType, clickTypeIn, player);
		
		if (!player.world.isRemote)
		{
			if (slotId == 0 || slotId == 1)
			{
				tryCreateSecurityTaggedItem();
			}
			
			if (slotId == 2 && stack.getItem() != Items.AIR)
			{
				Slot slot = getSlot(0);
				slot.putStack(new ItemStack(Items.AIR));
				slot = getSlot(1);
				slot.decrStackSize(1);
			}
			
			detectAndSendChanges();
		}
		
		if (slotId == 0 && onItemInserted != null)
		{
			onItemInserted.run();
		}
		
		return stack;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		getSlot(2).putStack(new ItemStack(Items.AIR));
		
		super.onContainerClosed(playerIn);
	}
}
