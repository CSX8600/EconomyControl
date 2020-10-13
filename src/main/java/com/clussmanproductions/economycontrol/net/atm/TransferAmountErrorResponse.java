package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;
import com.clussmanproductions.economycontrol.gui.GuiATM.TransferAmountData;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TransferAmountErrorResponse implements IMessage {
	public String errorMessage;

	@Override
	public void fromBytes(ByteBuf buf) {
		errorMessage = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, errorMessage);
	}
	
	public static class Handler implements IMessageHandler<TransferAmountErrorResponse, IMessage>
	{
		@Override
		public IMessage onMessage(TransferAmountErrorResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(TransferAmountErrorResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			TransferAmountData data = (TransferAmountData)atm.screenDataForScreen.get(Screens.TransferAmount);
			data.setError(message.errorMessage);
			atm.currentScreen = Screens.TransferAmount;
		}
	}
	
	public static void sendErrorResponse(String error, EntityPlayerMP player)
	{
		TransferAmountErrorResponse response = new TransferAmountErrorResponse();
		response.errorMessage = error;
		
		PacketHandler.INSTANCE.sendTo(response, player);
	}
}
