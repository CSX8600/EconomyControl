package com.clussmanproductions.economycontrol;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;

import org.apache.logging.log4j.Logger;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.data.government.GovernmentData;
import com.clussmanproductions.economycontrol.item.ItemMoney.MoneyTypes;
import com.clussmanproductions.economycontrol.proxy.CommonProxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;

@Mod(modid = EconomyControl.MODID, name = EconomyControl.NAME, version = EconomyControl.VERSION)
public class EconomyControl
{
    public static final String MODID = "economycontrol";
    public static final String NAME = "Economy Control";
    public static final String VERSION = "0.1.0";
    public static final CreativeTabs TAB = new CreativeTabs("economycontrol") {
		
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(ModItems.money, 1, MoneyTypes.Dollar_100.ordinal());
		}
	};

    public static Logger logger;
    
    @Mod.Instance
    public static EconomyControl instance;
    
    @SidedProxy(clientSide = "com.clussmanproductions.economycontrol.proxy.ClientProxy", serverSide = "com.clussmanproductions.economycontrol.proxy.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        logger = e.getModLog();
        
        proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e)
    {
        proxy.init(e);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
    	proxy.postInit(e);
    }
    
    public static String getWorldDataName(String folder, String name)
    {
    	return String.format("../EconomyControl/%s/%s", folder, name);
    }
    
    @EventHandler
	public static void onServerStart(FMLServerStartedEvent e)
	{		
		File file = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory(), "EconomyControl/BankAccounts");
		file.mkdirs();
		file = new File(file, "../BankAccountPlayers");
		file.mkdirs();
		file = new File(file, "../BankAccountHistory");
		file.mkdirs();
		file = new File(file, "../Governments");
		file.mkdirs();
		
		Calendar cal = Calendar.getInstance();
		BankAccountHistoryData.earliestYear = cal.get(Calendar.YEAR);
		
		String[] folders = file.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		
		for(String folder : folders)
		{
			try
			{
				int year = Integer.parseInt(folder);
				
				if (year < BankAccountHistoryData.earliestYear)
				{
					BankAccountHistoryData.earliestYear = year;
				}
			}
			catch(NumberFormatException ex) {}
		}
	}
	
	@EventHandler
	public static void onServerStop(FMLServerStoppingEvent e)
	{
		// Clear caches
		BankAccountData.clearCaches();
		BankAccountPlayerData.clearCaches();
		BankAccountHistoryData.clearCaches();
		GovernmentData.clearCaches();
	}
}
