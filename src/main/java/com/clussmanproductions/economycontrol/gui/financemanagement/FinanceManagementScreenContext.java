package com.clussmanproductions.economycontrol.gui.financemanagement;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

public class FinanceManagementScreenContext implements INBTSerializable<NBTTagCompound> {

	private boolean canManageCompanies;
	private boolean canCreateCompanies;
	private boolean canManageGovernments;
	private boolean canCreateGovernments;
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("canManageCompanies", canManageCompanies);
		tag.setBoolean("canCreateCompanies", canCreateCompanies);
		tag.setBoolean("canManageGovernments", canManageGovernments);
		tag.setBoolean("canCreateGovernments", canCreateGovernments);
		
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		canManageCompanies = nbt.getBoolean("canManageCompanies");
		canCreateCompanies = nbt.getBoolean("canCreateCompanies");
		canManageGovernments = nbt.getBoolean("canManageGovernments");
		canCreateGovernments = nbt.getBoolean("canCreateGovernments");
	}

	public boolean canManageCompanies() {
		return canManageCompanies;
	}

	public void setCanManageCompanies(boolean canManageCompanies) {
		this.canManageCompanies = canManageCompanies;
	}

	public boolean canManageGovernments() {
		return canManageGovernments;
	}

	public void setCanManageGovernments(boolean canManageGovernments) {
		this.canManageGovernments = canManageGovernments;
	}

	public boolean canCreateGovernments() {
		return canCreateGovernments;
	}

	public void setCanCreateGovernments(boolean canCreateGovernments) {
		this.canCreateGovernments = canCreateGovernments;
	}

	public boolean canCreateCompanies() {
		return canCreateCompanies;
	}

	public void setCanCreateCompanies(boolean canCreateCompanies) {
		this.canCreateCompanies = canCreateCompanies;
	}
}
