package com.clussmanproductions.economycontrol.net.financemanagement;

import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreen;
import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreenContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GetFinanceContextResponse implements IMessage {

	public FinanceManagementScreenContext context;
	@Override
	public void fromBytes(ByteBuf buf) {
		context = new FinanceManagementScreenContext();
		context.deserializeNBT(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, context.serializeNBT());
	}

	public static class Handler implements IMessageHandler<GetFinanceContextResponse, IMessage>
	{

		@Override
		public IMessage onMessage(GetFinanceContextResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(GetFinanceContextResponse message, MessageContext ctx)
		{
			GuiScreen shownScreen = Minecraft.getMinecraft().currentScreen;
			if (shownScreen != null && shownScreen instanceof FinanceManagementScreen)
			{
				((FinanceManagementScreen)shownScreen).setFinanceManagementScreenContext(message.context);
			}
		}
	}
}
