package com.clussmanproductions.economycontrol.item;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.item.render.SecurityTaggedItemRenderer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSecurityTaggedItem extends Item {

	public ItemSecurityTaggedItem()
	{
		super();
		setUnlocalizedName(EconomyControl.MODID + ".security_tagged_item");
		setRegistryName("security_tagged_item");
	}
	
	@SideOnly(Side.CLIENT)
	public void initModel()
	{
		setTileEntityItemStackRenderer(new SecurityTaggedItemRenderer());
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null)
		{
			UUID company = tag.getUniqueId("company");
			ItemStack internalItem = new ItemStack((NBTTagCompound)tag.getTag("internalItem"));
			long tagCost = tag.getLong("tagCost");
			long unitCost = tagCost / internalItem.getCount();
			double tagCostDouble = (double)(tagCost / 100D);
			double unitCostDouble = (double)(unitCost / 100D);
			NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
			String tagCostDisplay = format.format(tagCostDouble) + " (" + internalItem.getCount() + " @ " + format.format(unitCostDouble) + ")";
			tooltip.add("Company: " + company.toString());
			tooltip.add("Cost: " + tagCostDisplay);
			tooltip.addAll(internalItem.getTooltip(null, flagIn));
		}
	}
}
