package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RetrieveBankAccounts implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) {
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
	}

	public static class Handler implements IMessageHandler<RetrieveBankAccounts, IMessage>
	{
		@Override
		public IMessage onMessage(RetrieveBankAccounts message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(RetrieveBankAccounts message, MessageContext ctx)
		{
			try
			{				
				World world = ctx.getServerHandler().player.world;
				String playerName = ctx.getServerHandler().player.getName();
				
				List<BankAccountData> bankAccounts = new ArrayList<>();
				for(String bankAccountNumber : BankAccountPlayerData.getPersonalBankAccountsByPlayer(playerName, world))
				{
					bankAccounts.add(BankAccountData.getBankAccountByNumber(bankAccountNumber, world));
				}
				
				RetrieveBankAccountsResponse response = new RetrieveBankAccountsResponse();
				response.bankAccounts = bankAccounts;
				PacketHandler.INSTANCE.sendTo(response, ctx.getServerHandler().player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.getMessage(), ctx.getServerHandler().player);
			}
		}
	}
}
