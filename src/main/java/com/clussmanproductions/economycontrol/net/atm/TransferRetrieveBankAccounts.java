package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TransferRetrieveBankAccounts implements IMessage {
	public String currentBankAccountNumber;

	@Override
	public void fromBytes(ByteBuf buf) {
		currentBankAccountNumber = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, currentBankAccountNumber);
	}
	
	public static class Handler implements IMessageHandler<TransferRetrieveBankAccounts, IMessage>
	{
		@Override
		public IMessage onMessage(TransferRetrieveBankAccounts message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(TransferRetrieveBankAccounts message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				List<BankAccountData> personalAccounts = new ArrayList<>();
				for(String bankAccountNumber : BankAccountPlayerData.getPersonalBankAccountsByPlayer(player.getName(), world))
				{
					personalAccounts.add(BankAccountData.getBankAccountByNumber(bankAccountNumber, world));
				}
				
				TransferRetrieveBankAccountsResponse response = new TransferRetrieveBankAccountsResponse();
				response.currentBankAccount = message.currentBankAccountNumber;
				response.bankAccounts = personalAccounts;
				PacketHandler.INSTANCE.sendTo(response, player);
			}
			catch(Exception ex)
			{
				ErrorResponse error = new ErrorResponse();
				error.errorMessage = ex.toString();
				PacketHandler.INSTANCE.sendTo(error, ctx.getServerHandler().player);
			}
		}
	}
}
