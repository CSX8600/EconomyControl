package com.clussmanproductions.economycontrol.proxy;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.clussmanproductions.economycontrol.Config;
import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.ModBlocks;
import com.clussmanproductions.economycontrol.ModItems;
import com.clussmanproductions.economycontrol.blocks.BlockATM;
import com.clussmanproductions.economycontrol.gui.GuiProxy;
import com.clussmanproductions.economycontrol.item.ItemATM;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

@EventBusSubscriber
public class CommonProxy {
	public static Configuration config;
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> e)
	{
		for(Field field : ModBlocks.class.getFields())
		{
			try {
				Block block = (Block) field.getType().getConstructor(new Class<?>[0]).newInstance(new Object[0]);
				e.getRegistry().register(block);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				EconomyControl.logger.error("Failed to register block with field name " + field.getName(), e1);
			}
		}
		
		// No good way to auto-register TEs :(
		GameRegistry.registerTileEntity(ATMTileEntity.class, new ResourceLocation(EconomyControl.MODID, "atm"));
	}
	
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> e)
	{
		for(Field field : ModBlocks.class.getFields())
		{
			try {
				Block block = (Block)field.get(null);
				
				Item item = manuallyRegisterBlockItem(block);
				
				if (item == null)
				{
					item = new ItemBlock(block).setRegistryName(block.getRegistryName()).setUnlocalizedName(block.getUnlocalizedName());
				}
				
				e.getRegistry().register(item);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				EconomyControl.logger.error("Failed to register item for block with field name " + field.getName(), e1);
			}
		}
		
		for(Field field : ModItems.class.getFields())
		{
			try {
				Class<Item> itemClass = (Class<Item>)field.getType();
				Item newItem = itemClass.getConstructor(new Class<?>[0]).newInstance(new Object[0]);
				
				e.getRegistry().register(newItem);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				EconomyControl.logger.error("Failed to register item with field name " + field.getName(), e1);
			}
		}
	}
	
	private static Item manuallyRegisterBlockItem(Block block)
	{
		if (block == ModBlocks.atm)
		{
			return new ItemATM((BlockATM)block).setRegistryName(block.getRegistryName()).setUnlocalizedName(block.getUnlocalizedName());
		}
		
		return null;
	}
	
	public void preInit(FMLPreInitializationEvent e)
	{
		File directory = e.getModConfigurationDirectory();
		config = new Configuration(new File(directory.getPath(), "economycontrol.cfg"));
		Config.readConfig();
		
		PacketHandler.registerMessages(EconomyControl.MODID);
	}
	
	public void init(FMLInitializationEvent e)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(EconomyControl.instance, new GuiProxy());
		
		Permissions.registerPermissions();
	}
	
	public void postInit(FMLPostInitializationEvent e)
	{
		
	}

	public static class Permissions
	{
		public static final String breakAnyATM = "economycontrol.atm.breakAny";
		
		public static void registerPermissions()
		{
			PermissionAPI.registerNode(breakAnyATM, DefaultPermissionLevel.OP, "Can break any ATM regardless of whether or not they own it (owners can always break their own ATMs)");
		}
	}
}
