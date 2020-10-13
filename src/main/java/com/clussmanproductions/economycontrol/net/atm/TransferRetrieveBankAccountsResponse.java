package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;
import com.clussmanproductions.economycontrol.gui.GuiATM.TransferAccountSelectionData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TransferRetrieveBankAccountsResponse implements IMessage {
	public String currentBankAccount;
	public List<BankAccountData> bankAccounts = new ArrayList<>();
	
	@Override
	public void fromBytes(ByteBuf buf) {
		currentBankAccount = ByteBufUtils.readUTF8String(buf);
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		NBTTagList list = tag.getTagList("list", NBT.TAG_COMPOUND);
		for(NBTBase bankAccountData : list)
		{
			NBTTagCompound cmpd = (NBTTagCompound)bankAccountData;
			BankAccountData newData = new BankAccountData(null);
			newData.deserializeNBT(cmpd);
			bankAccounts.add(newData);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, currentBankAccount);
		NBTTagList list = new NBTTagList();
		for(BankAccountData ba : bankAccounts)
		{
			list.appendTag(ba.serializeNBT());
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("list", list);
		ByteBufUtils.writeTag(buf, compound);
	}
	
	public static class Handler implements IMessageHandler<TransferRetrieveBankAccountsResponse, IMessage>
	{
		@Override
		public IMessage onMessage(TransferRetrieveBankAccountsResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(TransferRetrieveBankAccountsResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			TransferAccountSelectionData data = (TransferAccountSelectionData)atm.screenDataForScreen.get(Screens.TransferAccountSelection);
			Optional<BankAccountData> current = message.bankAccounts.stream().filter(bad -> bad.getBankAccountNumber().equals(message.currentBankAccount)).findFirst();
			data.currentBankAccount = current.orElse(null);
			data.allBankAccounts = message.bankAccounts;
			
			atm.currentScreen = Screens.TransferAccountSelection;
		}
	}
}
