package com.clussmanproductions.economycontrol.net.financemanagement;

import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreenContext;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.proxy.CommonProxy;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.server.permission.PermissionAPI;

public class GetFinanceContext implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) { }

	@Override
	public void toBytes(ByteBuf buf) { }

	public static class Handler implements IMessageHandler<GetFinanceContext, IMessage>
	{

		@Override
		public IMessage onMessage(GetFinanceContext message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(GetFinanceContext message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			FinanceManagementScreenContext context = new FinanceManagementScreenContext();
			context.setCanManageCompanies(PermissionAPI.hasPermission(player, CommonProxy.Permissions.manageCompany));
			context.setCanCreateCompanies(PermissionAPI.hasPermission(player, CommonProxy.Permissions.createCompany));
			context.setCanCreateGovernments(PermissionAPI.hasPermission(player, CommonProxy.Permissions.createGovernment));
			context.setCanManageGovernments(PermissionAPI.hasPermission(player, CommonProxy.Permissions.manageGovernment));
			
			GetFinanceContextResponse response = new GetFinanceContextResponse();
			response.context = context;
			PacketHandler.INSTANCE.sendTo(response, player);
		}
	}
}
