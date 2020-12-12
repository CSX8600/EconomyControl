package com.clussmanproductions.economycontrol.item;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ItemSecurityTag extends Item {
	public ItemSecurityTag()
	{
		super();
		setUnlocalizedName(EconomyControl.MODID + ".security_tag");
		setRegistryName("security_tag");
		setCreativeTab(EconomyControl.TAB);
	}
	
	public void initModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
}
