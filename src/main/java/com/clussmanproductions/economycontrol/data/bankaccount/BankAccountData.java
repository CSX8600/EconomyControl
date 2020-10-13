package com.clussmanproductions.economycontrol.data.bankaccount;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;

public class BankAccountData extends WorldSavedData {

	private String bankAccountNumber = "";
	private String bankAccountName = "";
	private long balance = 0L;
	private BankAccountTypes bankAccountType = BankAccountTypes.Unknown;
	
	public BankAccountData(String bankAccountNumber) {
		super(bankAccountNumber);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		setBankAccountNumber(nbt.getString("number"));
		setBankAccountName(nbt.getString("name"));
		setBankAccountType(BankAccountTypes.getByID(nbt.getInteger("type")));
		setLongBalance(nbt.getLong("balance"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("number", getBankAccountNumber());
		compound.setString("name", getBankAccountName());
		compound.setInteger("type", getBankAccountType().getID());
		compound.setLong("balance", getLongBalance());
		return compound;
	}
	
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
		markDirty();
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
		markDirty();
	}

	public BankAccountTypes getBankAccountType() {
		return bankAccountType;
	}

	public void setBankAccountType(BankAccountTypes bankAccountType) {
		this.bankAccountType = bankAccountType;
		markDirty();
	}
	
	public long getLongBalance()
	{
		return balance;
	}
	
	public void setLongBalance(long balance)
	{
		this.balance = balance;
		markDirty();
	}
	
	public double getBalance()
	{
		return getLongBalance() / (double)100;
	}
	
	// Cached Data
	private static HashMap<String, BankAccountData> bankAccountsByNumber = new HashMap<>();
	
	public static BankAccountData getBankAccountByNumber(String number, World world)
	{
		if (!bankAccountsByNumber.containsKey(number))
		{
			// Load
			BankAccountData bankAccountData = (BankAccountData) world.getMapStorage().getOrLoadData(BankAccountData.class, EconomyControl.getWorldDataName("BankAccounts", number));
			
			if (bankAccountData != null)
			{
				bankAccountsByNumber.put(number, bankAccountData);
			}
		}
		
		return bankAccountsByNumber.get(number);
	}

	public static BankAccountData addBankAccount(BankAccountData bankAccount, World world)
	{
		Random rand = new Random();
		String accountNumber = "";
		do
		{
			for(int i = 0; i < 16; i++)
			{
				accountNumber += String.valueOf(rand.nextInt(9));
			}
		} while (bankAccountsByNumber.containsKey(accountNumber));
		
		BankAccountData newData = new BankAccountData(EconomyControl.getWorldDataName("BankAccounts", accountNumber));
		newData.setBankAccountName(bankAccount.getBankAccountName());
		newData.setBankAccountNumber(accountNumber);
		newData.setBankAccountType(bankAccount.getBankAccountType());
		
		world.getMapStorage().setData(EconomyControl.getWorldDataName("BankAccounts", accountNumber), newData);
		bankAccountsByNumber.put(accountNumber, newData);
		
		return newData;
	}

	public static void clearCaches()
	{
		bankAccountsByNumber.clear();
	}
}
