package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.net.PacketHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RetrieveHistories implements IMessage {
	public String bankAccountNumber;
	public int amount;
	public int skip;
	@Override
	public void fromBytes(ByteBuf buf) {
		bankAccountNumber = ByteBufUtils.readUTF8String(buf);
		amount = buf.readInt();
		skip = buf.readInt();
	}
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, bankAccountNumber);
		buf.writeInt(amount);
		buf.writeInt(skip);
	}
	
	public static class Handler implements IMessageHandler<RetrieveHistories, IMessage>
	{
		@Override
		public IMessage onMessage(RetrieveHistories message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(RetrieveHistories message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				List<History> history = new ArrayList<>();
				Calendar cal = Calendar.getInstance();
				int currentYear = cal.get(Calendar.YEAR);
				
				int amountSkipped = 0;
				int amountRemaining = message.amount + 1;
				
				for(int year = currentYear; year >= BankAccountHistoryData.earliestYear; year--)
				{
					for(int month = 11; month >= 0; month--)
					{
						BankAccountHistoryData historyForAccount = BankAccountHistoryData.getHistoryForAccount(year, month, message.bankAccountNumber, false, world);
						if (historyForAccount == null)
						{
							continue;
						}
						
						List<History> histories = historyForAccount.getTopHistories(amountRemaining + (message.skip - amountSkipped));
						
						if (histories.size() <= (message.skip - amountSkipped))
						{
							amountSkipped += histories.size();
							continue;
						}
						else if (message.skip - amountSkipped < histories.size())
						{
							int amountToSkip = message.skip - amountSkipped;
							
							history.addAll(histories.stream().skip(amountToSkip).collect(Collectors.toList()));
							amountSkipped += amountToSkip;
							amountRemaining -= histories.size() - amountToSkip;
						}
						
						if (amountRemaining == 0)
						{
							break;
						}
					}
					
					if (amountRemaining == 0)
					{
						break;
					}
				}
				
				boolean hasNext = history.size() > message.amount;
				history = history.stream().limit(message.amount).collect(Collectors.toList());
				
				RetrieveHistoriesResponse response = new RetrieveHistoriesResponse();
				response.histories = history;
				response.hasNext = hasNext;
				PacketHandler.INSTANCE.sendTo(response, player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.toString(), ctx.getServerHandler().player);
			}
		}
	}
}
