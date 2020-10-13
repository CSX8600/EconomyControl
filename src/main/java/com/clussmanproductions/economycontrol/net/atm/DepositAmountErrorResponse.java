package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.DepositData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DepositAmountErrorResponse implements IMessage {

	public String errorMessage;

	@Override
	public void fromBytes(ByteBuf buf) {
		errorMessage = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, errorMessage);
	}
	
	public static class Handler implements IMessageHandler<DepositAmountErrorResponse, IMessage>
	{
		@Override
		public IMessage onMessage(DepositAmountErrorResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(DepositAmountErrorResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			DepositData data = (DepositData)atm.screenDataForScreen.get(Screens.Deposit);
			data.setError(message.errorMessage);
			atm.currentScreen = Screens.Deposit;
		}		
	}
}
