package com.clussmanproductions.economycontrol;

import com.clussmanproductions.economycontrol.proxy.CommonProxy;

import net.minecraftforge.common.config.Configuration;

public class Config {
	private static final String CATEGORY_GENERAL = "general";
	
	public static String bankName = "MineBank";
	public static void readConfig()
	{
		Configuration cfg = CommonProxy.config;
		try
		{
			cfg.load();
			initGeneralConfig(cfg);
		}
		catch(Exception e)
		{
			EconomyControl.logger.error("Problem reading config file!", e);
		}
		finally
		{
			if (cfg.hasChanged())
			{
				cfg.save();
			}
		}
	}
	
	private static void initGeneralConfig(Configuration cfg)
	{
		cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
		bankName = cfg.getString("bankName", CATEGORY_GENERAL, bankName, "What is the name of bank to show on ATMs?");
	}
}
