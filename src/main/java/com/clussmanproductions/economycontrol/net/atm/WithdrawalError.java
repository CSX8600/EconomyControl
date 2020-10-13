package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.ErrorData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;
import com.clussmanproductions.economycontrol.gui.GuiATM.WithdrawalData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class WithdrawalError implements IMessage {

	public String message;
	@Override
	public void fromBytes(ByteBuf buf) {
		message = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, message);
	}

	public static class Handler implements IMessageHandler<WithdrawalError, IMessage>
	{
		@Override
		public IMessage onMessage(WithdrawalError message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(WithdrawalError message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			if (atm.currentScreen != Screens.Withdraw && atm.currentScreen != Screens.Loading)
			{
				ErrorData data = (ErrorData)atm.screenDataForScreen.get(Screens.Withdraw);
				data.errorMessage = "Withdrawal denied: " + message.message;
				atm.currentScreen = Screens.Error;
			}
			else
			{
				WithdrawalData data = (WithdrawalData)atm.screenDataForScreen.get(Screens.Withdraw);
				data.setError(message.message);
				atm.currentScreen = Screens.Withdraw;
			}
		}
	}
}
