package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData.BankAccountPlayerPair;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountShareTypes;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AddBankAccount implements IMessage
{

	public BankAccountData bankAccount;
	@Override
	public void fromBytes(ByteBuf buf) {
		bankAccount = new BankAccountData(null);
		bankAccount.deserializeNBT(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, bankAccount.serializeNBT());
	}

	public static class Handler implements IMessageHandler<AddBankAccount, IMessage>
	{
		@Override
		public IMessage onMessage(AddBankAccount message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(AddBankAccount message, MessageContext ctx)
		{
			try
			{
				World world = ctx.getServerHandler().player.world;
				BankAccountData savedData = BankAccountData.addBankAccount(message.bankAccount, world);
				BankAccountPlayerPair newPair = new BankAccountPlayerPair();
				newPair.setBankAccountNumber(savedData.getBankAccountNumber());
				newPair.setShareType(BankAccountShareTypes.Owner);
				BankAccountPlayerData.addBankAccountPlayerPair(ctx.getServerHandler().player.getName(), newPair, world);
			}
			catch(Exception ex)
			{
				EconomyControl.logger.error("Failed to add bank account!", ex);
			}
		}
	}
}
