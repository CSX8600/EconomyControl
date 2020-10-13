package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.InitializeFeeAccountData;
import com.clussmanproductions.economycontrol.gui.GuiATM.Screens;

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

public class InitializeRetrieveBankAccountsResponse implements IMessage {
	public List<BankAccountData> bankAccounts = new ArrayList<>();

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		NBTTagList list = tag.getTagList("list", NBT.TAG_COMPOUND);
		for(NBTBase tags : list)
		{
			NBTTagCompound bankAccountTag = (NBTTagCompound)tags;
			BankAccountData newBank = new BankAccountData(null);
			newBank.deserializeNBT(bankAccountTag);
			bankAccounts.add(newBank);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagList list = new NBTTagList();
		if (bankAccounts != null)
		{
			for(BankAccountData data : bankAccounts)
			{
				NBTTagCompound bankAccountCompound = data.serializeNBT();
				list.appendTag(bankAccountCompound);
			}
		}
		
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("list", list);
		ByteBufUtils.writeTag(buf, tag);
	}
	
	public static class Handler implements IMessageHandler<InitializeRetrieveBankAccountsResponse, IMessage>
	{
		@Override
		public IMessage onMessage(InitializeRetrieveBankAccountsResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(InitializeRetrieveBankAccountsResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			InitializeFeeAccountData data = (InitializeFeeAccountData)atm.screenDataForScreen.get(Screens.InitializeFeeAccount);
			data.bankAccounts = message.bankAccounts;
			atm.currentScreen = Screens.InitializeFeeAccount;
		}
	}
}
