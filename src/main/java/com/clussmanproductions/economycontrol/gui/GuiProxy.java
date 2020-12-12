package com.clussmanproductions.economycontrol.gui;

import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreen;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;
import com.clussmanproductions.economycontrol.tile.SecurityTaggingStationTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		switch(ID)
		{
			case GUI_IDs.SECURITY_TAGGING_STATION:
				if (te instanceof SecurityTaggingStationTileEntity)
				{
					return new ContainerSecurityTaggingStation(player.inventory, (SecurityTaggingStationTileEntity)te);
				}
				break;
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		switch(ID)
		{
			case GUI_IDs.ATM:
				if (te instanceof ATMTileEntity)
				{
					return new GuiATM(player, (ATMTileEntity)te);
				}
				break;
			case GUI_IDs.SECURITY_TAGGING_STATION:
				if (te instanceof SecurityTaggingStationTileEntity)
				{
					SecurityTaggingStationTileEntity taggingStation = (SecurityTaggingStationTileEntity)te;
					return new GuiSecurityTaggingStation(taggingStation, new ContainerSecurityTaggingStation(player.inventory, taggingStation));
				}
				break;
		}
		
		return null;
	}

	public static class GUI_IDs
	{
		public static final int ATM = 1;
		public static final int SECURITY_TAGGING_STATION = 2;
	}
}
