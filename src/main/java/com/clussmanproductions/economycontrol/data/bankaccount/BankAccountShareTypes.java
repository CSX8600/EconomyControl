package com.clussmanproductions.economycontrol.data.bankaccount;

public enum BankAccountShareTypes {
	Owner(0);
	private int id;
	BankAccountShareTypes(int id)
	{
		this.id = id;
	}
	
	public int getID()
	{
		return id;
	}
	
	public static BankAccountShareTypes getByID(int id)
	{
		for(BankAccountShareTypes type : BankAccountShareTypes.values())
		{
			if (type.id == id)
			{
				return type;
			}
		}
		
		return null;
	}
}
