package com.clussmanproductions.economycontrol.net.atm;

import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class InitializeRetrieveBankAccounts implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) { }

	@Override
	public void toBytes(ByteBuf buf) { }
	
	public static class Handler implements IMessageHandler<InitializeRetrieveBankAccounts, IMessage>
	{
		@Override
		public IMessage onMessage(InitializeRetrieveBankAccounts message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(InitializeRetrieveBankAccounts message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				List<BankAccountData> personalAccounts = BankAccountPlayerData.getPersonalBankAccountsByPlayer(player.getName(), world);
				
				InitializeRetrieveBankAccountsResponse response = new InitializeRetrieveBankAccountsResponse();
				response.bankAccounts = personalAccounts;
				PacketHandler.INSTANCE.sendTo(response, player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.toString(), ctx.getServerHandler().player);
			}
		}
	}
}
