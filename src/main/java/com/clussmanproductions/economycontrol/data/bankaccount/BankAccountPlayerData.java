package com.clussmanproductions.economycontrol.data.bankaccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class BankAccountPlayerData extends WorldSavedData
{
	private String player = "";
	private List<BankAccountPlayerPair> bankAccountPlayerPairs = new ArrayList<>();
	
	public BankAccountPlayerData(String playerName)
	{
		super(playerName);
	}
	
	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		setPlayer(nbt.getString("player"));
		NBTTagList pairs = nbt.getTagList("pairs", NBT.TAG_COMPOUND);
		for(NBTBase pair : pairs)
		{
			NBTTagCompound compound = (NBTTagCompound)pair;
			BankAccountPlayerPair pairObj = new BankAccountPlayerPair();
			pairObj.deserializeNBT(compound);
			bankAccountPlayerPairs.add(pairObj);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("player", getPlayer());
		NBTTagList pairs = new NBTTagList();
		for(BankAccountPlayerPair pair : bankAccountPlayerPairs)
		{
			pairs.appendTag(pair.serializeNBT());
		}
		compound.setTag("pairs", pairs);
		
		return compound;
	}
	
	public static class BankAccountPlayerPair implements INBTSerializable<NBTTagCompound>
	{
		private String bankAccountNumber;
		private BankAccountShareTypes shareType;

		public String getBankAccountNumber() {
			return bankAccountNumber;
		}

		public void setBankAccountNumber(String bankAccountNumber) {
			this.bankAccountNumber = bankAccountNumber;
		}

		public BankAccountShareTypes getShareType() {
			return shareType;
		}

		public void setShareType(BankAccountShareTypes shareType) {
			this.shareType = shareType;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("bankaccountnumber", getBankAccountNumber());
			nbt.setInteger("type", getShareType().getID());
			
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			setBankAccountNumber(nbt.getString("bankaccountnumber"));
			setShareType(BankAccountShareTypes.getByID(nbt.getInteger("type")));
		}
		
	}
	
	public boolean hasAccessTo(String bankAccountNumber)
	{
		return bankAccountPlayerPairs.stream().anyMatch(bapp -> bapp.bankAccountNumber.equals(bankAccountNumber));
	}
	
	// Cache
	private static HashMap<String, BankAccountPlayerData> playerDatasByPlayer = new HashMap<>();
	private static HashMap<String, List<String>> personalBankAccountsByPlayer = new HashMap<>();
	
	public static List<String> getPersonalBankAccountsByPlayer(String player, World world)
	{
		if (!personalBankAccountsByPlayer.containsKey(player))
		{
			loadBankAccountPlayer(player, world);
		}
		
		return personalBankAccountsByPlayer.get(player);
	}
	
	private static void loadBankAccountPlayer(String player, World world)
	{
		// Load
		BankAccountPlayerData bankAccountPlayer = (BankAccountPlayerData)world.getMapStorage().getOrLoadData(BankAccountPlayerData.class, EconomyControl.getWorldDataName("BankAccountPlayers", player));
		if (bankAccountPlayer == null)
		{
			bankAccountPlayer = new BankAccountPlayerData(EconomyControl.getWorldDataName("BankAccountPlayers", player));
			bankAccountPlayer.setPlayer(player);
			world.getMapStorage().setData(EconomyControl.getWorldDataName("BankAccountPlayers", player), bankAccountPlayer);
		}
		
		// Post init check/populate dictionaries
		for(BankAccountPlayerPair pair : bankAccountPlayer.bankAccountPlayerPairs)
		{
			BankAccountData bankAccountData = BankAccountData.getBankAccountByNumber(pair.getBankAccountNumber(), world);
			if (bankAccountData != null)
			{
				if (bankAccountData.getBankAccountType() == BankAccountTypes.PersonalChecking ||
						bankAccountData.getBankAccountType() == BankAccountTypes.PersonalSavings)
				{
					if (!personalBankAccountsByPlayer.containsKey(player))
					{
						personalBankAccountsByPlayer.put(player, new ArrayList<>());
					}
					
					personalBankAccountsByPlayer.get(player).add(pair.getBankAccountNumber());
				}
			}
		}		
		
		playerDatasByPlayer.put(player, bankAccountPlayer);
	}

	public static BankAccountPlayerData getBankAccountPlayerData(String player, World world)
	{
		if (!playerDatasByPlayer.containsKey(player))
		{
			loadBankAccountPlayer(player, world);
		}
		
		return playerDatasByPlayer.get(player);
	}
	
	public static void addBankAccountPlayerPair(String player, BankAccountPlayerPair pair, World world)
	{		
		BankAccountPlayerData playerData = getBankAccountPlayerData(player, world);
		if (playerData != null)
		{
			playerData.bankAccountPlayerPairs.add(pair);
			playerData.markDirty();
			
			BankAccountData bankAccount = BankAccountData.getBankAccountByNumber(pair.getBankAccountNumber(), world);
			BankAccountTypes type = bankAccount.getBankAccountType();
			if (type == BankAccountTypes.PersonalChecking || type == BankAccountTypes.PersonalSavings)
			{
				if (!personalBankAccountsByPlayer.containsKey(player))
				{
					personalBankAccountsByPlayer.put(player, new ArrayList<>());
				}
				
				personalBankAccountsByPlayer.get(player).add(pair.getBankAccountNumber());
			}
		}
	}
	
	public static void clearCaches()
	{
		playerDatasByPlayer.clear();
		personalBankAccountsByPlayer.clear();
	}
}
