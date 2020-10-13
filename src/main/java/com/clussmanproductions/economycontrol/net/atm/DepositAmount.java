package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.clussmanproductions.economycontrol.ModItems;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.item.ItemMoney;
import com.clussmanproductions.economycontrol.item.ItemMoney.MoneyTypes;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DepositAmount implements IMessage {

	public String account;
	public double amount;
	public BlockPos pos;
	
	@Override
	public void fromBytes(ByteBuf buf) {
		account = ByteBufUtils.readUTF8String(buf);
		amount = buf.readDouble();
		pos = BlockPos.fromLong(buf.readLong());
	}
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, account);
		buf.writeDouble(amount);
		buf.writeLong(pos.toLong());
	}
	
	public static class Handler implements IMessageHandler<DepositAmount, IMessage>
	{
		@Override
		public IMessage onMessage(DepositAmount message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(DepositAmount message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				long amount = Math.round(message.amount * 100);
				
				BankAccountData data = BankAccountData.getBankAccountByNumber(message.account, world);
				if (data == null)
				{
					DepositAmountErrorResponse error = new DepositAmountErrorResponse();
					error.errorMessage = "Bank Account doesn't exist!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				if (!handleItemStacks(player, amount))
				{
					DepositAmountErrorResponse error = new DepositAmountErrorResponse();
					error.errorMessage = "Not enough money!";
					PacketHandler.INSTANCE.sendTo(error, player);
					return;
				}
				
				data.setLongBalance(data.getLongBalance() + amount);
				
				String location = "UNKNOWN";
				TileEntity te = world.getTileEntity(message.pos);
				if (te != null && te instanceof ATMTileEntity)
				{
					ATMTileEntity atm = (ATMTileEntity)te;
					location = atm.getName();
				}
				
				Calendar cal = Calendar.getInstance();
				BankAccountHistoryData historyData = BankAccountHistoryData.getHistoryForAccount(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), data.getBankAccountNumber(), true, world);
				History history = historyData.createHistory(cal);
				historyData.setPerformer(player.getName(), history);
				historyData.setDescription("ATM Deposit @ " + location, history);
				historyData.setAmount(amount, history);
				
				SuccessResponse success = new SuccessResponse();
				PacketHandler.INSTANCE.sendTo(success, player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.toString(), ctx.getServerHandler().player);
			}
		}
		
		private boolean handleItemStacks(EntityPlayerMP player, long amount)
		{
			LinkedHashMap<MoneyTypes, List<ItemStack>> moneyOnPlayer = getStacksByMoneyType(player);
			List<Tuple<Integer, ItemStack>> stacksToDecrease = new ArrayList<>();
			List<ItemStack> stacksToAdd = new ArrayList<>();
			
			long remainingAmount = amount;
			boolean willBreakStack = false;
			for(Entry<MoneyTypes, List<ItemStack>> entry : moneyOnPlayer.entrySet())
			{
				for(ItemStack stack : entry.getValue())
				{
					Tuple<Integer, ItemStack> decreaseAmount = new Tuple<Integer, ItemStack>(0, stack);
					for(int i = 0; i < stack.getCount(); i++)
					{
						decreaseAmount = new Tuple<Integer, ItemStack>(decreaseAmount.getFirst() + 1, stack);
						long amountAfterReduction = remainingAmount - entry.getKey().getValue();
						remainingAmount -= entry.getKey().getValue();
						
						if (remainingAmount < 0)
						{
							willBreakStack = true;
							break;
						}
					}
					
					if (decreaseAmount.getFirst() > 0)
					{
						stacksToDecrease.add(decreaseAmount);
					}
					
					if (willBreakStack)
					{
						break;
					}
				}
				
				if (willBreakStack)
				{
					break;
				}
			}
			
			if (!willBreakStack && remainingAmount > 0)
			{
				return false;
			}
			
			if (willBreakStack)
			{
				stacksToAdd = ItemMoney.getMoneyStacksForAmount(Math.abs(remainingAmount));
			}
			
			for(Tuple<Integer, ItemStack> stackToDecrease : stacksToDecrease)
			{
				stackToDecrease.getSecond().grow(-stackToDecrease.getFirst());
			}
			
			for(ItemStack stackToAdd : stacksToAdd)
			{
				player.inventory.addItemStackToInventory(stackToAdd);
				
				if (stackToAdd.getCount() > 0)
				{
					player.dropItem(stackToAdd, true);
				}
			}
			
			return true;
		}
		
		private LinkedHashMap<MoneyTypes, List<ItemStack>> getStacksByMoneyType(EntityPlayerMP player)
		{
			LinkedHashMap<MoneyTypes, List<ItemStack>> map = new LinkedHashMap<>();
			for(MoneyTypes moneyType : MoneyTypes.values())
			{
				map.put(moneyType, new ArrayList<>());
			}
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);
				
				if (stackInSlot != null && stackInSlot.getItem() == ModItems.money)
				{
					MoneyTypes type = MoneyTypes.values()[stackInSlot.getMetadata()];
					map.get(type).add(stackInSlot);
				}
			}
			
			return map;
		}
	}
}
