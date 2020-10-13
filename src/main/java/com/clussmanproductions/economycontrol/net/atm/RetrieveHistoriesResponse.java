package com.clussmanproductions.economycontrol.net.atm;

import java.util.ArrayList;
import java.util.List;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.gui.GuiATM;
import com.clussmanproductions.economycontrol.gui.GuiATM.HistoryData;
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

public class RetrieveHistoriesResponse implements IMessage {
	public List<History> histories = new ArrayList<>();
	public boolean hasNext;

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound cmpd = ByteBufUtils.readTag(buf);
		BankAccountHistoryData dummy = new BankAccountHistoryData(null);
		
		NBTTagList list = cmpd.getTagList("list", NBT.TAG_COMPOUND);
		for(NBTBase base : list)
		{
			NBTTagCompound data = (NBTTagCompound)base;
			History history = dummy.emptyHistory();
			history.deserializeNBT(data);
			histories.add(history);
		}
		hasNext = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagList list = new NBTTagList();
		
		for(History history : histories)
		{
			list.appendTag(history.serializeNBT());
		}
		
		NBTTagCompound cmpd = new NBTTagCompound();
		cmpd.setTag("list", list);
		
		ByteBufUtils.writeTag(buf, cmpd);
		buf.writeBoolean(hasNext);
	}
	
	public static class Handler implements IMessageHandler<RetrieveHistoriesResponse, IMessage>
	{
		@Override
		public IMessage onMessage(RetrieveHistoriesResponse message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(RetrieveHistoriesResponse message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (!(mc.currentScreen instanceof GuiATM))
			{
				return;
			}
			
			GuiATM atm = (GuiATM)mc.currentScreen;
			if (atm.currentScreen != Screens.History)
			{
				return;
			}
			
			HistoryData data = (HistoryData)atm.screenDataForScreen.get(Screens.History);
			data.setHistories(message.histories, message.hasNext);
		}
	}
}
