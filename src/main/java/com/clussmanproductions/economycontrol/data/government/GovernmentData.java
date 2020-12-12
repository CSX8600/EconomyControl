package com.clussmanproductions.economycontrol.data.government;

import java.util.HashMap;
import java.util.UUID;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;

public class GovernmentData extends WorldSavedData {

	private UUID id = null;
	private String name = "";
	private long taxPercent = 0L;
	HashMap<String, PlayerSecurityContext> securityContextsByPlayer = new HashMap<>();
	
	public GovernmentData(String filePath)
	{
		super(filePath);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		id = nbt.getUniqueId("id");
		name = nbt.getString("name");
		taxPercent = nbt.getLong("taxPercent");
		
		int i = 0;
		for(PlayerSecurityContext psc : securityContextsByPlayer.values())
		{
			nbt.setTag("psc_" + i, psc.serializeNBT());
			i++;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setUniqueId("id", id);
		compound.setString("name", name);
		compound.setLong("taxPercent", taxPercent);
		
		int counter = 0;
		while(compound.hasKey("psc_" + counter))
		{
			NBTTagCompound tag = compound.getCompoundTag("psc_" + counter);
			PlayerSecurityContext context = new PlayerSecurityContext();
			context.deserializeNBT(tag);
			securityContextsByPlayer.put(context.getPlayer(), context);
			
			counter++;
		}
		
		return compound;
	}
	
	public static class PlayerSecurityContext implements INBTSerializable<NBTTagCompound>
	{
		private String player;
		private boolean canManageTax;
		private boolean canManageAccounts;
		private boolean canRequestMoney;
		private boolean canPayMoneyRequests;

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("player", player);
			compound.setBoolean("canManageTax", canManageTax);
			compound.setBoolean("canManageAccounts", canManageAccounts);
			compound.setBoolean("canRequestMoney", canRequestMoney);
			compound.setBoolean("canPayMoneyRequests", canPayMoneyRequests);
			return compound;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			player = nbt.getString("player");
			canManageTax = nbt.getBoolean("canManageTax");
			canManageAccounts = nbt.getBoolean("canManageAccounts");
			canRequestMoney = nbt.getBoolean("canRequestMoney");
			canPayMoneyRequests = nbt.getBoolean("canPayMoneyRequests");
		}

		public String getPlayer() {
			return player;
		}

		public void setPlayer(String player) {
			this.player = player;
		}

		public boolean canManageTax() {
			return canManageTax;
		}

		public void setCanManageTax(boolean canManageTax) {
			this.canManageTax = canManageTax;
		}

		public boolean isCanManageAccounts() {
			return canManageAccounts;
		}

		public void setCanManageAccounts(boolean canManageAccounts) {
			this.canManageAccounts = canManageAccounts;
		}

		public boolean isCanRequestMoney() {
			return canRequestMoney;
		}

		public void setCanRequestMoney(boolean canRequestMoney) {
			this.canRequestMoney = canRequestMoney;
		}

		public boolean isCanPayMoneyRequests() {
			return canPayMoneyRequests;
		}

		public void setCanPayMoneyRequests(boolean canPayMoneyRequests) {
			this.canPayMoneyRequests = canPayMoneyRequests;
		}
	}

	// Cached data
	private static HashMap<UUID, GovernmentData> govsByID = new HashMap<>();
	
	public GovernmentData getFederalGovernmentByID(UUID id, World world)
	{
		if (!govsByID.containsKey(id))
		{
			load(id, world);
		}
		
		return govsByID.get(id);
	}
	
	private void load(UUID id, World world)
	{
		GovernmentData fedGovData = (GovernmentData)world.getMapStorage().getOrLoadData(GovernmentData.class, EconomyControl.getWorldDataName("Governments", id.toString()));
		
		if (fedGovData != null)
		{
			govsByID.put(id, fedGovData);
		}
	}
	
	public static void clearCaches()
	{
		govsByID.clear();
	}
}
