package com.clussmanproductions.economycontrol.gui.financemanagement.screens.gov;

import java.io.IOException;

import com.clussmanproductions.economycontrol.gui.windowed.Button;
import com.clussmanproductions.economycontrol.gui.windowed.Screen;
import com.clussmanproductions.economycontrol.gui.windowed.Window;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;

public class SelectGovernment extends Screen {

	public SelectGovernment(Window parentWindow, int x, int y, int width, int height) {
		super(parentWindow, x, y, width, height);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initGui() {
		super.initGui();
		
		int horizontalCenter = width / 2;
		int verticalCenter = height / 2;
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		// TODO Auto-generated method stub
		super.actionPerformed(button);
	}
}
