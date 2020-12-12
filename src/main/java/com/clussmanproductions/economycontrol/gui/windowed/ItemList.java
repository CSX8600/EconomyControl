package com.clussmanproductions.economycontrol.gui.windowed;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ItemList implements IControl {

	private FontRenderer fontRenderer;
	private int x;
	private int y;
	private int width;
	private int height;
	
	private int skip;
	private int maxShownItems;
	
	private final int itemHeight = 11;
	
	ArrayList<Item> items = new ArrayList<>();
	private int selectedIndex = -1;
	private Item selectedItem = null;
	
	public ItemList(FontRenderer fontRenderer, int x, int y, int width, int height)
	{
		this.fontRenderer = fontRenderer;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		maxShownItems = height / itemHeight;
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableTexture2D();
		GlStateManager.color(1, 1, 1, 1F);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder builder = tess.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		
		
		
		tess.draw();
		
		GlStateManager.enableTexture2D();
	}
	
	private void drawBackground(BufferBuilder builder)
	{
		builder.pos(x + width, y, 0).color(0, 0, 0, 0.5F).endVertex();
		builder.pos(x, y, 0).color(0, 0, 0, 0.5F).endVertex();
		builder.pos(x, y + height, 0).color(0, 0, 0, 0.5F).endVertex();
		builder.pos(x + width, y + height, 0).color(0, 0, 0, 0.5F).endVertex();
	}
	
	private void drawSelectionBox(BufferBuilder builder)
	{
		if (selectedIndex != -1 && selectedIndex >= skip && selectedIndex <= skip + maxShownItems)
		{
			
		}
	}
	
	private void drawScrollBar(BufferBuilder builder)
	{
		if (maxShownItems <= items.size())
		{
			return;
		}
	}
	
	public void addItem(Item item)
	{
		items.add(item);
	}

	public Item getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Item selectedItem) {
		if (!items.contains(selectedItem))
		{
			items.add(selectedItem);
		}
		
		this.selectedItem = selectedItem;
		selectedIndex = items.indexOf(selectedItem);
	}

	public static class Item
	{
		private String text;
		private Object value;
		
		public Item(String text, Object value)
		{
			this.text = text;
			this.value = value;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}
}
