package com.clussmanproductions.economycontrol.data.bankaccount;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class BankAccountHistoryData extends WorldSavedData
{	
	public static int earliestYear = 0;
	private TreeMap<Calendar, History> histories = new TreeMap<>(Collections.reverseOrder());
	
	public BankAccountHistoryData(String filePath) {
		super(filePath);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("list", NBT.TAG_COMPOUND);
		
		for(NBTBase listItem : list)
		{
			NBTTagCompound compound = (NBTTagCompound)listItem;
			History history = new History();
			history.deserializeNBT(compound);
			histories.put(history.transactionDate, history);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		
		for(History history : histories.values())
		{
			list.appendTag(history.serializeNBT());
		}
		
		compound.setTag("list", list);
		return compound;
	}

	public class History implements INBTSerializable<NBTTagCompound>
	{
		private Calendar transactionDate;
		private String performer;
		private String description;
		private long amount;
		public Calendar getTransactionDate() {
			return transactionDate;
		}
		protected void setTransactionDate(Calendar transactionDate) {
			this.transactionDate = transactionDate;
		}
		public String getPerformer() {
			return performer;
		}
		protected void setPerformer(String performer) {
			this.performer = performer;
		}
		public String getDescription() {
			return description;
		}
		protected void setDescription(String description) {
			this.description = description;
		}
		public long getAmount() {
			return amount;
		}
		protected void setAmount(long amount) {
			this.amount = amount;
		}
		@Override
		public NBTTagCompound serializeNBT() {
			long date = transactionDate.toInstant().toEpochMilli();
			NBTTagCompound tag = new NBTTagCompound();
			
			tag.setLong("date", date);
			tag.setString("performer", performer);
			tag.setString("description", description);
			tag.setLong("amount", amount);
			
			return tag;
		}
		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			long date = nbt.getLong("date");
			transactionDate = Calendar.getInstance();
			transactionDate.setTimeInMillis(date);
			performer = nbt.getString("performer");
			description = nbt.getString("description");
			amount = nbt.getLong("amount");
		}
	}

	public List<History> getHistoriesForRange(Calendar inclusiveStart, Calendar inclusiveEnd)
	{
		ArrayList<History> histories = new ArrayList<>();
		for(Calendar date : this.histories.keySet().stream().filter(d -> d.after(inclusiveStart) && d.before(inclusiveEnd)).collect(Collectors.toList()))
		{
			histories.add(this.histories.get(date));
		}
		
		return histories;
	}
	
	public List<History> getTopHistories(int amt)
	{
		List<History> historiesToReturn = new ArrayList<>();
		if (amt > histories.size())
		{
			amt = histories.size();
		}
		
		History[] historyArray = histories.values().toArray(new History[0]);
		for(int i = 0; i < amt; i++)
		{
			historiesToReturn.add(historyArray[i]);
		}
		
		return historiesToReturn;
	}
	
	public History createHistory(Calendar transactionDate)
	{
		History newHistory = new History();
		newHistory.setTransactionDate(transactionDate);
		histories.put(transactionDate, newHistory);
		markDirty();
		
		return newHistory;
	}
	
	public History emptyHistory()
	{
		return new History();
	}
	
	public void setTransactionDate(Calendar newDate, History history)
	{
		histories.remove(history.getTransactionDate());
		history.setTransactionDate(newDate);
		histories.put(newDate, history);
		markDirty();
	}
	
	public void setPerformer(String performer, History history)
	{
		history.setPerformer(performer);
		markDirty();
	}
	
	public void setDescription(String description, History history)
	{
		history.setDescription(description);
		markDirty();
	}
	
	public void setAmount(long amount, History history)
	{
		history.setAmount(amount);
		markDirty();
	}
	
	// Cached
	private static HashMap<Integer, HashMap<Integer, HashMap<String, BankAccountHistoryData>>> cachedHistories = new HashMap<>(); 
	
	public static BankAccountHistoryData getHistoryForAccount(int year, int month, String accountNumber, boolean createIfMissing, World world)
	{
		if (!cachedHistories.containsKey(year))
		{
			cachedHistories.put(year, new HashMap<>());
		}
		
		if (!cachedHistories.get(year).containsKey(month))
		{
			cachedHistories.get(year).put(month, new HashMap<>());
		}
		
		if (!cachedHistories.get(year).get(month).containsKey(accountNumber))
		{
			BankAccountHistoryData historyData = (BankAccountHistoryData) world.getPerWorldStorage().getOrLoadData(BankAccountHistoryData.class, EconomyControl.getWorldDataName("BankAccountHistories/" + year + "/" + month, accountNumber));
			
			if (historyData == null && !createIfMissing)
			{
				return null;
			}
			else if (historyData == null)
			{
				String savedDataName = EconomyControl.getWorldDataName("BankAccountHistories/" + year + "/" + month, accountNumber);
				historyData = new BankAccountHistoryData(savedDataName);
				String folder = savedDataName.substring(savedDataName.indexOf('/') + 1);
				folder = folder.substring(0, folder.lastIndexOf('/'));
				File savedFolder = world.getSaveHandler().getWorldDirectory();
				savedFolder = new File(savedFolder, folder);
				savedFolder.mkdirs();
				
				world.getPerWorldStorage().setData(savedDataName, historyData);
			}
			
			cachedHistories.get(year).get(month).put(accountNumber, historyData);
		}
		
		return cachedHistories.get(year).get(month).get(accountNumber);
	}

	public static void clearCaches()
	{
		cachedHistories.clear();
	}
}
