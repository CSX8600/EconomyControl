package com.clussmanproductions.economycontrol.event;

import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreen;
import com.clussmanproductions.economycontrol.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class KeyboardEventHandler {
	@SubscribeEvent
	public static void onKeyPress(KeyInputEvent e)
	{
		if (!ClientProxy.financeManagementKey.isPressed() || Minecraft.getMinecraft().currentScreen != null)
		{
			return;
		}
		
		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new FinanceManagementScreen()));
	}
}
