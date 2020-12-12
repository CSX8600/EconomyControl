package com.clussmanproductions.economycontrol.gui;

public class GuiUtilities {
	public static boolean isMouseInRegion(int x1, int y1, int x2, int y2, int pX, int pY)
	{
		return pX >= x1 && pX <= x2 && pY >= y1 && pY <= y2;
	}
}
