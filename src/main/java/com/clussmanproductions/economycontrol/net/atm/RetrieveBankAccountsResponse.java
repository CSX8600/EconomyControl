package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.BankAccountSelectionData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RetrieveBankAccountsResponse implements IMessage {

	public List<BankAccountData> bankAccounts = new ArrayList<>();
	@Override
	public void fromBytes(ByteBuf buf) {
		int bankAccountCount = buf.readInt();
		for(int i = 0; i < bankAccountCount; i++)
		{
			NBTTagCompound tag = ByteBufUtils.readTag(buf);
			BankAccountData data = new BankAccountData(null);
			data.deserializeNBT(tag);
			bankAccounts.add(data);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (bankAccounts != null)
		{
			buf.writeInt(bankAccounts.size());
			for(BankAccountData data : bankAccounts)
			{
				ByteBufUtils.writeTag(buf, data.serializeNBT());
			}
		}
		else
		{
			buf.writeInt(0);
		}
	}

	public static class Handler implements IMessageHandler<RetrieveBankAccountsResponse, IMessage>
	{
		@Override
		public IMessage onMessage(RetrieveBankAccountsResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(RetrieveBankAccountsResponse message, MessageContext ctx)
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			if (!(minecraft.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atmGui = (GuiATM)minecraft.currentScreen;
			BankAccountSelectionData selectionData = (BankAccountSelectionData)atmGui.screenDataForScreen.get(Screens.BankAccountSelection);
			selectionData.bankAccounts = message.bankAccounts;
			atmGui.currentScreen = Screens.BankAccountSelection;
		}
	}
}
