package com.clussmanproductions.economycontrol.item.render;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class SecurityTaggedItemRenderer extends TileEntityItemStackRenderer {
	IBakedModel frame = null;
	Exception bakedException;
	@Override
	public void renderByItem(ItemStack itemStackIn) {
		NBTTagCompound compound = itemStackIn.getTagCompound();
		if (compound.hasKey("internalItem"))
		{
			ItemStack stack = new ItemStack((NBTTagCompound)compound.getTag("internalItem"));
			
			if (frame == null)
			{
				try {
					IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(EconomyControl.MODID, "item/security_tagged_item_frame"));
					frame = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
				} 
				catch (Exception e)
				{
					if (!e.equals(bakedException))
					{
						EconomyControl.logger.error("Failed to bake security tagged item frame!", e);
						bakedException = e;
					}
				}
			}
			
			if (frame != null)
			{
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, frame);
			}
			
			GlStateManager.scale(0.9, 0.9, 0.9);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.FIXED);
			GlStateManager.scale(1.1, 1.1, 1.1);
			//Minecraft.getMinecraft().getRenderItem().renderItem(stack, frame);
		}
	}
	
	
}
