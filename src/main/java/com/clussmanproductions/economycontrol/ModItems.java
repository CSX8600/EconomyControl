package com.clussmanproductions.economycontrol;

import com.clussmanproductions.economycontrol.item.ItemMoney;

import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("economycontrol")
public class ModItems {
	@ObjectHolder("money")
	public static ItemMoney money;
	
	public static void initModels()
	{
		money.initModel();
	}
}
