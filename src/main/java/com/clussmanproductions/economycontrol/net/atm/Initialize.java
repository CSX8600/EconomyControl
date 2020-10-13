package com.clussmanproductions.economycontrol.net.atm;

import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Initialize implements IMessage {

	public BlockPos pos;
	@Override
	public void fromBytes(ByteBuf buf)
	{ 
		pos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf)
	{ 
		buf.writeLong(pos.toLong());
	}
	
	public static class Handler implements IMessageHandler<Initialize, IMessage>
	{
		@Override
		public IMessage onMessage(Initialize message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(Initialize message, MessageContext ctx)
		{
			try
			{
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				
				TileEntity te = world.getTileEntity(message.pos);
				if (!(te instanceof ATMTileEntity))
				{
					ErrorResponse.sendError("ATM no longer exists at this location", player);
				}
				
				ATMTileEntity atm = (ATMTileEntity)te;
				BankAccountData selectedAccount = BankAccountData.getBankAccountByNumber(atm.getFeeAccountNumber(), world);
				if (selectedAccount == null)
				{
					selectedAccount = new BankAccountData(null);
				}
				
				InitializeResponse response = new InitializeResponse();
				response.selectedAccount = selectedAccount;
				PacketHandler.INSTANCE.sendTo(response, player);
			}
			catch(Exception ex)
			{
				ErrorResponse.sendError(ex.toString(), ctx.getServerHandler().player);
			}
		}
	}
}
