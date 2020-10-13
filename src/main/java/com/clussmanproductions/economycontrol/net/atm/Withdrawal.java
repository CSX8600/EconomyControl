package com.clussmanproductions.economycontrol.net.atm;

import java.util.Calendar;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountPlayerData;
import com.clussmanproductions.economycontrol.item.ItemMoney;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Withdrawal implements IMessage {

	public String bankAccountNumber;
	public double amount;
	public long fee;
	public BlockPos pos;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		bankAccountNumber = ByteBufUtils.readUTF8String(buf);
		amount = buf.readDouble();
		fee = buf.readLong();
		pos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, bankAccountNumber);
		buf.writeDouble(amount);
		buf.writeLong(fee);
		buf.writeLong(pos.toLong());
	}

	public static class Handler implements IMessageHandler<Withdrawal, IMessage>
	{

		@Override
		public IMessage onMessage(Withdrawal message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(Withdrawal message, MessageContext ctx)
		{			
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				// Security check
				String playerName = ctx.getServerHandler().player.getName();
				
				BankAccountPlayerData playerData = BankAccountPlayerData.getBankAccountPlayerData(playerName, world);
				if (!playerData.hasAccessTo(message.bankAccountNumber))
				{
					WithdrawalError error = new WithdrawalError();
					error.message = "Insufficient Account Access!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				if (message.amount <= 0)
				{
					WithdrawalError error = new WithdrawalError();
					error.message = "Number must be greater than 0!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				BankAccountData data = BankAccountData.getBankAccountByNumber(message.bankAccountNumber, world);
				BankAccountData feeData = null;
				TileEntity te = world.getTileEntity(message.pos);
				String location = "UNKNOWN";
				if (te != null && te instanceof ATMTileEntity)
				{
					ATMTileEntity atm = (ATMTileEntity)te;
					feeData = BankAccountData.getBankAccountByNumber(atm.getFeeAccountNumber(), world);
					location = atm.getName();
				}
				
				if (data == null)
				{
					WithdrawalError error = new WithdrawalError();
					error.message = "Account is missing!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				if (data.getBalance() < message.amount + (message.fee / 100D))
				{
					WithdrawalError error = new WithdrawalError();
					error.message = "Insufficient Funds!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				long newBalance = data.getLongBalance() - Math.round(message.amount * 100);
				
				if (feeData != null)
				{
					newBalance -= message.fee;
				}
				
				data.setLongBalance(newBalance);
				
				Calendar cal = Calendar.getInstance();
				BankAccountHistoryData historyData = BankAccountHistoryData.getHistoryForAccount(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), data.getBankAccountNumber(), true, world);
				History history = historyData.createHistory(cal);
				historyData.setPerformer(player.getName(), history);
				historyData.setDescription("ATM Withdrawal @ " + location, history);
				historyData.setAmount(-Math.round(message.amount * 100), history);
				
				if (feeData != null && message.fee > 0)
				{
					cal = Calendar.getInstance();
					cal.add(Calendar.MILLISECOND, 1);
					history = historyData.createHistory(cal);
					historyData.setPerformer(player.getName(), history);
					historyData.setDescription("ATM Withdrawal Fee @ " + location, history);
					historyData.setAmount(-message.fee, history);
					
					cal.add(Calendar.MILLISECOND, -1);
				}
				
				// Give money to player
				long remainingBalance = Math.round(message.amount * 100);
				List<ItemStack> stacksToGive = ItemMoney.getMoneyStacksForAmount(remainingBalance);
				
				for(ItemStack stackToGive : stacksToGive)
				{
					player.inventory.addItemStackToInventory(stackToGive);
					
					if (stackToGive.getCount() > 0)
					{
						player.dropItem(stackToGive, true);
					}
				}
				
				// Give money to fee collector
				if (feeData != null && message.fee > 0)
				{
					feeData.setLongBalance(feeData.getLongBalance() + message.fee);
					
					historyData = BankAccountHistoryData.getHistoryForAccount(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), feeData.getBankAccountNumber(), true, world);
					history = historyData.createHistory(cal);
					historyData.setPerformer(player.getName(), history);
					historyData.setDescription("ATM Withdrawal Fee Collection @ " + location, history);
					historyData.setAmount(message.fee, history);
				}
				
				SuccessResponse success = new SuccessResponse();
				PacketHandler.INSTANCE.sendTo(success, player);
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
