package com.clussmanproductions.economycontrol.net.taggingstation;

import java.util.Optional;
import java.util.UUID;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.gui.ContainerSecurityTaggingStation;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ValueUpdated implements IMessage
{
	public BlockPos tePos;
	public UUID company = null;
	public Optional<Long> tagCost;
	@Override
	public void fromBytes(ByteBuf buf) {
		tePos = BlockPos.fromLong(buf.readLong());
		
		long tagCost = buf.readLong();
		
		if (tagCost != Long.MIN_VALUE)
		{
			this.tagCost = Optional.of(tagCost);
		}
		
		long major = buf.readLong();
		long minor = buf.readLong();
		
		if (major != 0 && minor != 0)
		{
			company = new UUID(major, minor);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(tePos.toLong());
		buf.writeLong(tagCost != null && tagCost.isPresent() ? tagCost.get() : Long.MIN_VALUE);
		
		if (company != null)
		{
			buf.writeLong(company.getMostSignificantBits());
			buf.writeLong(company.getLeastSignificantBits());
		}
		else
		{
			buf.writeLong(0);
			buf.writeLong(0);
		}
	}
	
	public static class Handler implements IMessageHandler<ValueUpdated, IMessage>
	{

		@Override
		public IMessage onMessage(ValueUpdated message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(ValueUpdated message, MessageContext ctx)
		{
			Container container = ctx.getServerHandler().player.openContainer;
			if (!(container instanceof ContainerSecurityTaggingStation))
			{
				EconomyControl.logger.warn("Tried to update value on server for security tagging station, but container is not security tagging station!");
				return;
			}
			
			ContainerSecurityTaggingStation c = (ContainerSecurityTaggingStation)container;
			c.company = message.company;
			c.tagCost = message.tagCost;
			c.tryCreateSecurityTaggedItem();
		}
	}
}
