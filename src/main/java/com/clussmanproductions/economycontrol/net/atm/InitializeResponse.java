package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.InitializeData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class InitializeResponse implements IMessage {
	public BankAccountData selectedAccount;

	@Override
	public void fromBytes(ByteBuf buf) {
		selectedAccount = new BankAccountData(null);
		selectedAccount.deserializeNBT(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, selectedAccount.serializeNBT());
	}
	
	public static class Handler implements IMessageHandler<InitializeResponse, IMessage>
	{
		@Override
		public IMessage onMessage(InitializeResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(InitializeResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			InitializeData data = (InitializeData)atm.screenDataForScreen.get(Screens.Initialize);
			data.selectedAccount = message.selectedAccount;
			atm.currentScreen = Screens.Initialize;
		}
	}
}
