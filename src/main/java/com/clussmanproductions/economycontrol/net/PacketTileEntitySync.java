package com.clussmanproductions.economycontrol.net;

import com.clussmanproductions.economycontrol.tile.SyncableTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTileEntitySync implements IMessage {

	public BlockPos pos;
	public NBTTagCompound tag;
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		tag = ByteBufUtils.readTag(buf);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		ByteBufUtils.writeTag(buf, tag);
	}
	
	public static class Handler implements IMessageHandler<PacketTileEntitySync, IMessage>
	{
		@Override
		public IMessage onMessage(PacketTileEntitySync message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(PacketTileEntitySync message, MessageContext ctx)
		{
			World world = ctx.getServerHandler().player.world;
			TileEntity te = world.getTileEntity(message.pos);
			
			if (te == null || !(te instanceof SyncableTileEntity))
			{
				return;
			}
			
			((SyncableTileEntity)te).handleTagOnServer(message.tag);
		}
	}
}
