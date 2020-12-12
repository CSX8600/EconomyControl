package com.clussmanproductions.economycontrol.gui.windowed;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.gui.GuiUtilities;
import com.clussmanproductions.economycontrol.gui.financemanagement.FinanceManagementScreen;
import com.google.common.collect.HashMultimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

public class Window {
	private FontRenderer fontRenderer;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean isActive = true;
	private boolean isHidden = false;
	private String text;
	private UUID windowID = UUID.randomUUID();
	private HashMultimap<WindowEvents, Consumer<Window>> eventListeners = HashMultimap.create();
	private FinanceManagementScreen parentScreen;
	private Screen screen;
	
	private boolean windowDragging = false;
	private int windowDragRelativeX;
	private int windowDragRelativeY;
	
	public Window(FinanceManagementScreen parentScreen, FontRenderer fontRenderer, int x, int y, int width, int height)
	{
		this.parentScreen = parentScreen;
		this.fontRenderer = fontRenderer;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void draw(int mouseX, int mouseY, float partialTicks)
	{
		if (isHidden())
		{
			return;
		}
		
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.disableTexture2D();
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder builder = tess.getBuffer();
		
		float color = isActive() ? 0.8F : 0.5F;
		
		// Window border
		builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		builder.pos(x, y + 10, 0).color(0, 0, color, 1).endVertex();
		builder.pos(x, y + height, 0).color(0, 0, color, 1).endVertex();
		
		builder.pos(x, y + height, 0).color(0, 0, color, 1).endVertex();
		builder.pos(x + width, y + height, 0).color(0, 0, color, 1).endVertex();
		
		builder.pos(x + width, y + height, 0).color(0, 0, color, 1).endVertex();
		builder.pos(x + width, y + 10, 0).color(0, 0, color, 1).endVertex();
		
		tess.draw();
		
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/finance_management_screen.png"));
		int textureOffset = isActive() ? 0 : 1;

		// Window title bar
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		builder.pos(x + (width / 2), y, 0).tex((width / 2) / 256D, (0 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x, y, 0).tex(0, (0 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x, y + 10, 0).tex(0, (10 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x + (width / 2), y + 10, 0).tex((width / 2) / 256D, (10 + 10 * textureOffset) / 256D).endVertex();
		
		builder.pos(x + width, y, 0).tex(200 / 256D, (0 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x + (width / 2), y, 0).tex((200 - width / 2) / 256D, (0 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x + (width / 2), y + 10, 0).tex((200 - width / 2) / 256D, (10 + 10 * textureOffset) / 256D).endVertex();
		builder.pos(x + width, y + 10, 0).tex(200 / 256D, (10 + 10 * textureOffset) / 256D).endVertex();
		
		tess.draw();
		
		if (getText() != null)
		{
			fontRenderer.drawStringWithShadow(getText(), x + 4, y + 1, 0xFFFFFF);
		}
		
		int fontWidth = fontRenderer.getStringWidth("X");
		int fontColor = GuiUtilities.isMouseInRegion(x + width - fontWidth - 2, y + 2, x + width - 2, y + 2 + fontRenderer.FONT_HEIGHT, mouseX, mouseY) ?
				0xFF0000 :
				0x880000;
		
		int xX = x + width - fontWidth - 2;
		
		fontRenderer.drawString("X", xX, y + 2, fontColor);
		
		fontWidth = fontRenderer.getStringWidth("-");
		fontColor = GuiUtilities.isMouseInRegion(xX - 2 - fontWidth, y + 2, xX - 2, y + 2 + fontRenderer.FONT_HEIGHT, mouseX, mouseY) ?
				0x0000FF :
				0x000022;
		
		fontRenderer.drawString("-", xX - 2 - fontWidth, y + 2, fontColor);
		
		GlStateManager.color(1, 1, 1);
		screen.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void mouseClick(int mouseX, int mouseY, int mouseButton) throws IOException
	{		
		if (isHidden())
		{
			return;
		}
		
		// Is on header?
		if (GuiUtilities.isMouseInRegion(x, y, x + width, y + 10, mouseX, mouseY))
		{
			// Did click close?
			if (GuiUtilities.isMouseInRegion(x + width - 6 - 2, y + 2, x + width - 2, y + 2 + fontRenderer.FONT_HEIGHT, mouseX, mouseY))
			{
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				for(Consumer<Window> consumer : eventListeners.get(WindowEvents.Close))
				{
					consumer.accept(this);
				}
				
				return;
			}
			
			// Did click minimize?
			if (GuiUtilities.isMouseInRegion(x + width - 6 - 2 - 2 - 6, y + 2, x + width - 6 - 2 - 2, y + 2 + fontRenderer.FONT_HEIGHT, mouseX, mouseY))
			{
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				for(Consumer<Window> consumer : eventListeners.get(WindowEvents.Minimize))
				{
					consumer.accept(this);
				}
				
				return;
			}
			
			windowDragging = true;
			windowDragRelativeX = mouseX - x;
			windowDragRelativeY = mouseY - y;
		}
		
		if (screen != null)
		{
			screen.screenMouseClicked(mouseX - screen.getX(), mouseY - screen.getY(), mouseButton);
		}
	}
	
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
	{
		if (isHidden() || !isActive() || !windowDragging)
		{
			return;
		}
		
		int newX = mouseX - windowDragRelativeX;
		int newY = mouseY - windowDragRelativeY;
		
		setX(newX);
		setY(newY);
		
		if (screen != null)
		{
			screen.setX(newX);
			screen.setY(newY + 10);
		}
	}
	
	public void mouseReleased(int mouseX, int mouseY, int state)
	{
		if (isHidden() || !isActive())
		{
			return;
		}
		
		windowDragging = false;
	}
	
	public void keyTyped(char typedChar, int keyCode) throws IOException
	{
		if (isHidden())
		{
			return;
		}
		
		if (screen != null)
		{
			screen.screenKeyTyped(typedChar, keyCode);
		}
	}
	
	public void addListener(WindowEvents windowState, Consumer<Window> handler)
	{
		eventListeners.put(windowState, handler);
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public UUID getWindowID() {
		return windowID;
	}

	public void setWindowID(UUID windowID) {
		this.windowID = windowID;
	}

	public Screen getScreen() {
		return screen;
	}

	public FinanceManagementScreen getFinanceManagementScreen()
	{
		return parentScreen;
	}
	
	public <T extends Screen> T generateScreen(Class<T> clazz)
	{
		T newScreen;
		try {
			newScreen = (T)clazz.getDeclaredConstructor(Window.class, int.class, int.class, int.class, int.class).newInstance(this, x, y + 10, width, height - 10);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			new Exception("Tried to create the requested Screen, but got an exception!", e).printStackTrace();
			return null;
		}
		this.screen = newScreen;
		
		return newScreen;
	}
	
	public enum WindowEvents
	{
		Close,
		Minimize
	}
}
