package com.clussmanproductions.economycontrol.net.atm;

import java.util.Calendar;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TransferAmount implements IMessage {

	public String bankAccountFrom;
	public String bankAccountTo;
	public double amount;
	public BlockPos pos;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		bankAccountFrom = ByteBufUtils.readUTF8String(buf);
		bankAccountTo = ByteBufUtils.readUTF8String(buf);
		amount = buf.readDouble();
		pos = BlockPos.fromLong(buf.readLong());
	}
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, bankAccountFrom);
		ByteBufUtils.writeUTF8String(buf, bankAccountTo);
		buf.writeDouble(amount);
		buf.writeLong(pos.toLong());
	}
	
	public static class Handler implements IMessageHandler<TransferAmount, IMessage>
	{
		@Override
		public IMessage onMessage(TransferAmount message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(TransferAmount message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				// Security check
				if (!BankAccountPlayerData.getBankAccountPlayerData(player.getName(), world).hasAccessTo(message.bankAccountFrom))
				{
					TransferAmountErrorResponse.sendErrorResponse("No access to From Account!", player);
					return;
				}
				
				BankAccountData fromAccount = BankAccountData.getBankAccountByNumber(message.bankAccountFrom, world);
				BankAccountData toAccount = BankAccountData.getBankAccountByNumber(message.bankAccountTo, world);
				
				if (fromAccount == null || toAccount == null)
				{
					TransferAmountErrorResponse.sendErrorResponse("Either To or From account doesn't exist!", player);
					return;
				}
				
				long transferAmount = (long)Math.round(message.amount * 100);
				
				if (transferAmount < 0)
				{
					TransferAmountErrorResponse.sendErrorResponse("Amount must be greater than 0!", player);
				}
				
				if (fromAccount.getLongBalance() < transferAmount)
				{
					TransferAmountErrorResponse.sendErrorResponse("Insufficient funds!", player);
					return;
				}
				
				fromAccount.setLongBalance(fromAccount.getLongBalance() - transferAmount);
				toAccount.setLongBalance(toAccount.getLongBalance() + transferAmount);
				
				String location = "UNKNOWN";
				TileEntity te = world.getTileEntity(message.pos);
				if (te instanceof ATMTileEntity)
				{
					ATMTileEntity atm = (ATMTileEntity)te;
					location = atm.getName();
				}
				
				Calendar cal = Calendar.getInstance();
				BankAccountHistoryData historyData = BankAccountHistoryData.getHistoryForAccount(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), fromAccount.getBankAccountNumber(), true, world);
				History history = historyData.createHistory(cal);
				historyData.setPerformer(player.getName(), history);
				historyData.setDescription("ATM Transfer @ " + location + " to " + toAccount.getBankAccountName(), history);
				historyData.setAmount(-transferAmount, history);
				
				historyData = BankAccountHistoryData.getHistoryForAccount(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), toAccount.getBankAccountNumber(), true, world);
				history = historyData.createHistory(cal);
				historyData.setPerformer(player.getName(), history);
				historyData.setDescription("ATM Transfer @ " + location + " from " + fromAccount.getBankAccountName(), history);
				historyData.setAmount(transferAmount, history);
				
				SuccessResponse success = new SuccessResponse();
				PacketHandler.INSTANCE.sendTo(success, player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.toString(), ctx.getServerHandler().player);
			}
		}
	}
}
