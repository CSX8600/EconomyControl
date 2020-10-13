package com.clussmanproductions.economycontrol;

import com.clussmanproductions.economycontrol.blocks.BlockATM;
import com.clussmanproductions.economycontrol.blocks.BlockATMUpper;

import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(EconomyControl.MODID)
public class ModBlocks {
	@ObjectHolder("atm")
	public static BlockATM atm;
	@ObjectHolder("atm_upper")
	public static BlockATMUpper atm_upper;
	
	public static void initModels()
	{
		atm.initModel();
	}
}
