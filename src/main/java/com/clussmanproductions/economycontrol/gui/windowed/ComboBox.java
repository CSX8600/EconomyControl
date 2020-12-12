package com.clussmanproductions.economycontrol.gui.windowed;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ComboBox extends Gui implements IControl {
	private int x;
	private int y;
	private int width = 200;
	private int height = 20;
	private int maxItemsShown = 3;
	private boolean isOpen = false;
	private int scrollSkip = 0;
	private int mouseXScroll = 0;
	private int mouseYScroll = 0;
	private boolean isScrollClicked = false;
	private ArrayList<ComboBoxItem> items = new ArrayList<>();
	private ComboBoxItem selectedItem = null;
	private FontRenderer fontRenderer;
	
	public ComboBox(FontRenderer fontRenderer, int x, int y, int width, int height)
	{
		this.fontRenderer = fontRenderer;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void draw(int mouseX, int mouseY, float partialTicks)
	{
		GlStateManager.color(255, 255, 255);
		GlStateManager.disableLighting();
		GlStateManager.translate(0, 0, 1000);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/combobox.png"));
				
		int hoverState = isMouseHovering(mouseX, mouseY) ? 1 : 0;
		
		drawScaledCustomSizeModalRect(x, y, 0, 20 * hoverState, width / 2, 20, width / 2, height, 256, 256);
		drawScaledCustomSizeModalRect(x + width / 2, y, 200 - width / 2, 20 * hoverState, width / 2, 20, width / 2, height, 256, 256);
		
		if (selectedItem != null)
		{
			String shownText = selectedItem.getText();
			int stringWidth = fontRenderer.getStringWidth(shownText);
			
			if (stringWidth > width - 21)
			{
				shownText = fontRenderer.trimStringToWidth(shownText.substring(0, shownText.length() - 3) + "...", width - 21);
			}
			
			fontRenderer.drawString(shownText, x + 1, y + 1 + (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFF);
		}
		
		if (isOpen)
		{
			boolean hasScrollBar = items.size() >= maxItemsShown;
			int forStart = scrollSkip;
			int forEnd = (hasScrollBar ? scrollSkip + maxItemsShown : items.size());
			
			if (forEnd > items.size())
			{
				forEnd = items.size();
			}
			
			for(int i = forStart; i < forEnd; i++)
			{
				int itemY = y + height + height * (i - forStart);
				int itemHoverState = isMouseHovering(mouseX, mouseY, x, itemY, hasScrollBar ? width - 10 : width, height) ? 0 : 1;
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/combobox.png"));
				GlStateManager.color(255, 255, 255);
				drawScaledCustomSizeModalRect(x, itemY, 0, 40 + itemHoverState * 20, width / 2, 20, width / 2, height, 256, 256);
				drawScaledCustomSizeModalRect(x + width / 2, itemY, 200 - width / 2, 40 + 20 * itemHoverState, width / 2, 20, width / 2, height, 256, 256);
				
				ComboBoxItem item = items.get(i);
				String shownText = item.getText();
				int stringWidth = fontRenderer.getStringWidth(shownText);
				
				int maxTextWidth = hasScrollBar ? width - 11 : width - 1;
				
				if (stringWidth > maxTextWidth)
				{
					shownText = fontRenderer.trimStringToWidth(shownText.substring(0, shownText.length() - 3) + "...", maxTextWidth);
				}

				fontRenderer.drawString(shownText, x + 1, itemY + 1 + (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFF);
			}
			
			if (hasScrollBar)
			{
				ScrollBarData scrollBarData = new ScrollBarData();
				
				GlStateManager.disableTexture2D();
				GlStateManager.color(0, 0, 0);
				
				Tessellator tess = Tessellator.getInstance();
				BufferBuilder builder = tess.getBuffer();
				
				builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
				builder.pos(scrollBarData.scrollBarX + 5, y + height, zLevel).endVertex();;
				builder.pos(scrollBarData.scrollBarX + 5, y + scrollBarData.shownItemsHeight + height, zLevel).endVertex();
				tess.draw();
				
				builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
				builder.pos(scrollBarData.scrollBarX, scrollBarData.scrollBarY, zLevel).endVertex();
				builder.pos(scrollBarData.scrollBarX, scrollBarData.scrollBarY + scrollBarData.scrollBarHeight, zLevel).endVertex();
				builder.pos(scrollBarData.scrollBarX + 10, scrollBarData.scrollBarY + scrollBarData.scrollBarHeight, zLevel).endVertex();
				builder.pos(scrollBarData.scrollBarX + 10, scrollBarData.scrollBarY, zLevel).endVertex();
				tess.draw();
				
				GlStateManager.enableTexture2D();
			}
		}
		
		GlStateManager.translate(0, 0, -1000);
		GlStateManager.enableLighting();
	}
	
	private boolean isMouseHovering(int mouseX, int mouseY)
	{
		return isMouseHovering(mouseX, mouseY, x, y, width, height);
	}
	
	private boolean isMouseHovering(int mouseX, int mouseY, int x, int y, int width, int height)
	{
		return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
	}
	
	public boolean mouseClick(int mouseX, int mouseY, int mouseButton)
	{
		boolean handled = false;
		if (isOpen)
		{
			boolean hasScrollBar = items.size() > maxItemsShown;
			int forEnd = scrollSkip + maxItemsShown;
			
			if (forEnd > items.size())
			{
				forEnd = items.size();
			}
			
			for(int i = 0; i < forEnd; i++)
			{
				int itemY = y + height + height * i;
				
				if (isMouseHovering(mouseX, mouseY, x, itemY, hasScrollBar ? width - 10 : width, height))
				{
					ComboBoxItem selectedItem = items.get(i + scrollSkip);
					this.selectedItem = selectedItem;
					isOpen = false;
					handled = true;
					break;
				}
			}
			
			if (!handled && hasScrollBar)
			{
				ScrollBarData data = new ScrollBarData();
				
				if (isMouseHovering(mouseX, mouseY, data.scrollBarX, data.scrollBarY, 10, data.shownItemsHeight))
				{
					mouseXScroll = mouseX;
					mouseYScroll = mouseY;
					isScrollClicked = true;
					
					handled=true;
				}
			}
		}
		
		if (isMouseHovering(mouseX, mouseY))
		{
			isOpen = !isOpen;
			handled = true;
		}
		
		if (!handled)
		{
			isOpen = false;
		}
		
		return isOpen;
	}
	
	@Override
	public void mouseClickMoved(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
	{
		if (isScrollClicked)
		{
			ScrollBarData data = new ScrollBarData();
			
			// Most obvious cases
			if (mouseY < y + height)
			{
				scrollSkip = 0;
			}
			else if (mouseY >= y + height + data.shownItemsHeight)
			{
				scrollSkip = items.size() - maxItemsShown;
			}
			else // Less obvious cases
			{
				int scrollBarMouseLocation = mouseY - (y + height);
				scrollSkip = scrollBarMouseLocation / data.scrollBarHeight;
			}
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state)
	{
		mouseXScroll = 0;
		mouseYScroll = 0;
		isScrollClicked = false;
	}
		
	public void handleMouseInput()
	{
		if (isOpen)
		{
			boolean hasScrollBar = items.size() > maxItemsShown;
			
			if (hasScrollBar)
			{
				ScrollBarData data = new ScrollBarData();
				
				int scrollWheel = Integer.signum(Mouse.getDWheel());
				
				if (scrollWheel != 0)
				{
					if (scrollWheel == -1 && scrollSkip < data.numberOfSteps)
					{
						scrollSkip++;
					}

					if (scrollWheel == 1 && scrollSkip > 0)
					{
						scrollSkip--;
					}
				}
			}
		}
	}
	
	public void addComboBoxItem(ComboBoxItem item)
	{
		items.add(item);
	}

	public boolean isOpen()
	{
		return isOpen;
	}
	
	public ComboBoxItem getSelectedItem()
	{
		return selectedItem;
	}
	
	public static class ComboBoxItem
	{
		private String text;
		private Object value;
		
		public ComboBoxItem(String text, Object value)
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

	private class ScrollBarData
	{
		int numberOfSteps = items.size() - maxItemsShown;
		int scrollBarHeight = (maxItemsShown - numberOfSteps) * height;
		int scrollBarX = x + width - 10;
		int scrollBarY = y + height + height * scrollSkip;
		int shownItemsHeight = maxItemsShown * height;
	}
}
