package com.clussmanproductions.economycontrol.proxy;

import org.lwjgl.input.Keyboard;

import com.clussmanproductions.economycontrol.ModBlocks;
import com.clussmanproductions.economycontrol.ModItems;
import com.clussmanproductions.economycontrol.item.render.CustomItemModelLoader;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	public static KeyBinding financeManagementKey;
	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		ModelLoaderRegistry.registerLoader(new CustomItemModelLoader());
	}
	
	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
		
		financeManagementKey = new KeyBinding("key.economycontrol.financemanagement.toggle", Keyboard.KEY_F8, "key.economycontrol.category.name");
		ClientRegistry.registerKeyBinding(financeManagementKey);
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent e)
	{
		ModBlocks.initModels();
		ModItems.initModels();
	}
}
