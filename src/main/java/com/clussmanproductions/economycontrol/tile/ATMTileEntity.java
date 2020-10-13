package com.clussmanproductions.economycontrol.tile;

import com.clussmanproductions.economycontrol.ModBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ATMTileEntity extends SyncableTileEntity {
	private String name = "";
	private long withdrawalFeeAmount;
	private String feeAccountNumber = "";
	private String owner = "";
	private boolean isInitialized = false;
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		name = compound.getString("name");
		withdrawalFeeAmount = compound.getLong("withdrawalFeeAmount");
		isInitialized = compound.getBoolean("initialized");
		feeAccountNumber = compound.getString("feeAccountNumber");
		owner = compound.getString("owner");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("name", name);
		compound.setLong("withdrawalFeeAmount", withdrawalFeeAmount);
		compound.setBoolean("initialized", isInitialized);
		compound.setString("feeAccountNumber", feeAccountNumber);
		compound.setString("owner", owner);
		return super.writeToNBT(compound);
	}

	@Override
	public NBTTagCompound getTagForServer() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("name", name);
		tag.setLong("withdrawalFeeAmount", withdrawalFeeAmount);
		tag.setBoolean("initialized", isInitialized);
		tag.setString("feeAccountNumber", feeAccountNumber);
		
		return tag;
	}

	@Override
	public void handleTagOnServer(NBTTagCompound tag) {
		name = tag.getString("name");
		withdrawalFeeAmount = tag.getLong("withdrawalFeeAmount");
		isInitialized = tag.getBoolean("initialized");
		feeAccountNumber = tag.getString("feeAccountNumber");
		
		markDirty();
		world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), 2);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return newSate.getBlock() != ModBlocks.atm;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
		markDirty();
	}
	
	public long getWithdrawalFeeAmount()
	{
		return withdrawalFeeAmount;
	}
	
	public void setWithdrawalFeeAmount(long amount)
	{
		withdrawalFeeAmount = amount;
		markDirty();
	}
	
	public boolean getIsInitialized()
	{
		return isInitialized;
	}
	
	public void setIsInitialized(boolean initialized)
	{
		isInitialized = initialized;
		markDirty();
	}

	public String getFeeAccountNumber() {
		return feeAccountNumber;
	}

	public void setFeeAccountNumber(String feeAccountNumber) {
		this.feeAccountNumber = feeAccountNumber;
		markDirty();
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
		markDirty();
	}
}
