package com.clussmanproductions.economycontrol.gui.windowed;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.clussmanproductions.economycontrol.EconomyControl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public abstract class Screen extends GuiScreen {
	private Window parentWindow;
	private int x;
	private int y;
	private ResourceLocation backgroundTexture = new ResourceLocation(EconomyControl.MODID, "textures/gui/expando.png");
	private int textureWidth;
	private int textureHeight;
	protected HashSet<IControl> controls = new HashSet<>();
	
	public Screen(Window parentWindow, int x, int y, int width, int height)
	{
		this.parentWindow = parentWindow;
		this.x = x;
		this.y = y;
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
	}
	
	protected boolean useDefaultBackground() { return true; }
	
	@Override
	public void initGui() {
		super.initGui();
		
		for(Button control : controls
								.stream()
								.filter(c -> c instanceof Button)
								.map(c -> (Button)c)
								.collect(Collectors.toList()))
		{
			if (!buttonList.contains(control))
			{
				buttonList.add(control);
			}
		}
	}
	
	@Override
	public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.translate(x, y, 0);
		
		if (useDefaultBackground())
		{
			mc.getTextureManager().bindTexture(backgroundTexture);
			
			drawModalRectWithCustomSizedTexture(
					0,
					0,
					0,
					0,
					width / 2,
					height / 2, 
					256, 
					256);
			
			drawModalRectWithCustomSizedTexture(
					width / 2,
					0,
					200 - width / 2,
					0, 
					width / 2, 
					height / 2,
					256, 
					256);
			
			drawModalRectWithCustomSizedTexture(
					0,
					height / 2,
					0,
					200 - height / 2, 
					width / 2,
					height / 2,
					256,
					256);
			
			drawModalRectWithCustomSizedTexture(
					width / 2,
					height / 2,
					200 - width / 2,
					200 - height / 2,
					width / 2,
					height / 2,
					256,
					256);
		}
		
		controls.forEach(c -> c.draw(mouseX, mouseY, partialTicks));
		performDraw(mouseX - getX(), mouseY - getY(), partialTicks);
		
		GlStateManager.translate(-x, -y, 0);
	}
	
	public void screenMouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		mouseClicked(mouseX, mouseY, mouseButton);
		controls.forEach(c -> c.mouseClick(mouseX, mouseY, mouseButton));
	}
	
	public void screenMouseClickMoved(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
	{
		mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		controls.forEach(c -> c.mouseClickMoved(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
	}
	
	public void mouseReleased(int mouseX, int mouseY, int state)
	{
		mouseReleased(mouseX, mouseY, state);
		controls.forEach(c -> c.mouseReleased(mouseX, mouseY, state));
	}
	
	public void screenKeyTyped(char typedChar, int keyCode) throws IOException
	{
		keyTyped(typedChar, keyCode);
		controls.forEach(c -> c.keyTyped(typedChar, keyCode));
	}
	
	protected Window getParentWindow() { return parentWindow; }
	
	protected void performDraw(int mouseX, int mouseY, float partialTicks) {};

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
}
