package com.clussmanproductions.economycontrol.data.bankaccount;

public enum BankAccountTypes {
	Unknown(-1),
	Federal(0),
	Local(1),
	Company(2),
	PersonalChecking(3),
	PersonalSavings(4);
	
	private int id;
	BankAccountTypes(int id)
	{
		this.id = id;
	}
	
	public int getID()
	{
		return id;
	}
	
	public static BankAccountTypes getByID(int id)
	{
		for(BankAccountTypes type : BankAccountTypes.values())
		{
			if (type.id == id)
			{
				return type;
			}
		}
		
		return null;
	}
	
	public String getFriendlyName()
	{
		switch(id)
		{
			case 0:
				return "Federal";
			case 1:
				return "Local";
			case 2:
				return "Company";
			case 3:
				return "Checking";
			case 4:
				return "Savings";
			default:
				return "";
		}
	}
}
