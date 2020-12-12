package com.clussmanproductions.economycontrol.gui.windowed;

import net.minecraft.client.gui.FontRenderer;

public class Label implements IControl {
	
	private FontRenderer fontRenderer;
	private int x;
	private int y;
	private String text;
	private int color;
	
	public Label(FontRenderer fontRenderer, int x, int y, String text, int color)
	{
		this.fontRenderer = fontRenderer;
		this.x = x;
		this.y = y;
		this.text = text;
		this.color = color;
	}
	
	private int getWidth()
	{
		return fontRenderer.getStringWidth(getText());
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		fontRenderer.drawString(getText(), getX(), getY(), getColor());
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

}
