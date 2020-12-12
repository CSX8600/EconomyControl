package com.clussmanproductions.economycontrol.item.render;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public class SecurityTaggedItemModel implements IModel {
	
	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.of(
				new ResourceLocation(EconomyControl.MODID, "items/dark_gray_plastic"),
				new ResourceLocation(EconomyControl.MODID, "items/light_gray_plastic"));
		
//		return Collections.EMPTY_LIST;
	}
	
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new PerspectiveMapWrapper(new SecurityTaggedItemBakedModel(), SecurityTaggedItemBakedModel.getTransforms());
	}

	public static class SecurityTaggedItemBakedModel implements IBakedModel
	{
		@Override
		public boolean isGui3d() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isBuiltInRenderer() {
			// TODO Auto-generated method stub
			return true;
		}
		
		@Override
		public boolean isAmbientOcclusion() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public TextureAtlasSprite getParticleTexture() {
			return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}
		
		@Override
		public ItemOverrideList getOverrides() {
			// TODO Auto-generated method stub
			return ItemOverrideList.NONE;
		}
		
		public static ImmutableMap<TransformType, TRSRTransformation> getTransforms()
		{
			return ImmutableMap.<TransformType, TRSRTransformation>builder()
					.put(TransformType.FIRST_PERSON_LEFT_HAND,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.25F, 0), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.FIRST_PERSON_RIGHT_HAND,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(1F, 0.25F, 0), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.THIRD_PERSON_LEFT_HAND,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0F, 0.5F, 0.5F), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.THIRD_PERSON_RIGHT_HAND,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.5F, 0.5F), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.GROUND,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.5F, 0.5F), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.FIXED,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.5F, 0.5F), new Vector3f(0.5F, 0.5F, 0.5F))))
					.put(TransformType.GUI,
							new TRSRTransformation(new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0.5F, 0.5F, 0), new Vector3f(0.8F, 0.8F, 0.8F))))
					.build();
		}
	}
}
