package com.clussmanproductions.economycontrol.item;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.ModItems;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMoney extends Item {
	public ItemMoney()
	{
		super();
		setRegistryName("money");
		setUnlocalizedName(EconomyControl.MODID + ".money");
		setHasSubtypes(true);
		setCreativeTab(EconomyControl.TAB);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab == EconomyControl.TAB)
		{
			for(int i = 0; i < MoneyTypes.values().length; i++)
			{
				items.add(new ItemStack(this, 1, i));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void initModel()
	{
		for(int i = 0; i < MoneyTypes.values().length; i++)
		{
			MoneyTypes moneyType = MoneyTypes.values()[i];
			
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation("economycontrol:" + moneyType.getName(), "inventory"));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		MoneyTypes moneyType = MoneyTypes.values()[stack.getMetadata()];
		return String.format("item.%s.money.%s", EconomyControl.MODID, moneyType.getName());
	}
	
	public enum MoneyTypes
	{
		Penny("cent_1", 1),
		Nickel("cent_5", 5),
		Dime("cent_10", 10),
		Quarter("cent_25", 25),
		Dollar_1("dollar_1", 100),
		Dollar_5("dollar_5", 500),
		Dollar_10("dollar_10", 1000),
		Dollar_20("dollar_20", 2000),
		Dollar_50("dollar_50", 5000),
		Dollar_100("dollar_100", 10000);
		
		private String name;
		private long value;
		
		MoneyTypes(String name, long value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String getName()
		{
			return name;
		}
		
		public long getValue()
		{
			return value;
		}
	}

	public static List<ItemStack> getMoneyStacksForAmount(long amount)
	{
		ArrayList<ItemStack> stacksToGive = new ArrayList<>();
		
		int moneys = (int)(amount / 10000);
		amount -= moneys * 10000;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_100.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 5000);
		amount -= moneys * 5000;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_50.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 2000);
		amount -= moneys * 2000;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_20.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 1000);
		amount -= moneys * 1000;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_10.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 500);
		amount -= moneys * 500;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_5.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 100);
		amount -= moneys * 100;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dollar_1.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / (double)25);
		amount -= moneys * 25;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Quarter.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 10);
		amount -= moneys * 10;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Dime.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)(amount / 5);
		amount -= moneys * 5;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Nickel.ordinal());
			stacksToGive.add(stack);
		}
		
		moneys = (int)amount;
		amount -= moneys;
		if (moneys > 0)
		{
			ItemStack stack = new ItemStack(ModItems.money, moneys, ItemMoney.MoneyTypes.Penny.ordinal());
			stacksToGive.add(stack);
		}
		
		return stacksToGive;
	}
}
