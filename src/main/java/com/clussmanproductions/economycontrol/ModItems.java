package com.clussmanproductions.economycontrol;

import com.clussmanproductions.economycontrol.item.ItemMoney;
import com.clussmanproductions.economycontrol.item.ItemSecurityTag;
import com.clussmanproductions.economycontrol.item.ItemSecurityTaggedItem;

import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("economycontrol")
public class ModItems {
	@ObjectHolder("money")
	public static ItemMoney money;
	@ObjectHolder("security_tag")
	public static ItemSecurityTag security_tag;
	@ObjectHolder("security_tagged_item")
	public static ItemSecurityTaggedItem security_tagged_item;
	
	public static void initModels()
	{
		money.initModel();
		security_tag.initModel();
		security_tagged_item.initModel();
	}
}
