package com.clussmanproductions.economycontrol.item.render;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class CustomItemModelLoader implements ICustomModelLoader {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation.getResourceDomain().equals(EconomyControl.MODID) && modelLocation.getResourcePath().equals("security_tagged_item");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return new SecurityTaggedItemModel();
	}

}
