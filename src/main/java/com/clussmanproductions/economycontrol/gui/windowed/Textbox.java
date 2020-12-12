package com.clussmanproductions.economycontrol.gui.windowed;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class Textbox extends GuiTextField implements IControl {

	public Textbox(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
		super(componentId, fontrendererObj, x, y, par5Width, par6Height);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawTextBox();
	}

	@Override
	public boolean keyTyped(char keyedChar, int typedKey) {
		return textboxKeyTyped(keyedChar, typedKey);
	}
	
	@Override
	public boolean mouseClick(int mouseX, int mouseY, int mouseButton) {
		return mouseClicked(mouseX, mouseY, mouseButton);
	}
}
