package com.clussmanproductions.economycontrol.gui.windowed;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class Button extends GuiButtonExt implements IControl {

	Minecraft mc = Minecraft.getMinecraft();
	
	public Button(int id, int xPos, int yPos, int width, int height, String displayString) {
		super(id, xPos, yPos, width, height, displayString);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawButton(mc, mouseX, mouseY, partialTicks);
	}
}
