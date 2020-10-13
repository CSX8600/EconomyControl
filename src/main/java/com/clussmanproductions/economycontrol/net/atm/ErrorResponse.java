package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.ErrorData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ErrorResponse implements IMessage {

	public String errorMessage;
	@Override
	public void fromBytes(ByteBuf buf) {
		errorMessage = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, errorMessage);
	}
	
	public static void sendError(String message, EntityPlayerMP player)
	{
		ErrorResponse response = new ErrorResponse();
		response.errorMessage = message;
		PacketHandler.INSTANCE.sendTo(response, player);
	}
	
	public static class Handler implements IMessageHandler<ErrorResponse, IMessage>
	{

		@Override
		public IMessage onMessage(ErrorResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(ErrorResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			
			ErrorData data = (ErrorData)atm.screenDataForScreen.get(Screens.Error);
			data.errorMessage = message.errorMessage;
			atm.currentScreen = Screens.Error;
		}
	}
}
