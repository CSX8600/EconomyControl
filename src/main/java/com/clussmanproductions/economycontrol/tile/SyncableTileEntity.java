package com.clussmanproductions.economycontrol.tile;

import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.net.PacketTileEntitySync;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class SyncableTileEntity extends TileEntity {
	public abstract NBTTagCompound getTagForServer();	
	public abstract void handleTagOnServer(NBTTagCompound tag);
	
	public void syncWithServer()
	{
		PacketTileEntitySync sync = new PacketTileEntitySync();
		sync.pos = getPos();
		sync.tag = getTagForServer();
		PacketHandler.INSTANCE.sendToServer(sync);
	}
}
