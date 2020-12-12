package com.clussmanproductions.economycontrol.gui.windowed;

public class ScrollBar {
	private int x;
	private int y;
	private int width;
	private int height;
	private int itemAmount;
	private int maxShownItems;
	private int currentStep;
	private int scrollBarWidth;
	
	public ScrollBar(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void draw()
	{
		
	}
	
	private class ScrollBarInfo
	{
		public int scrollBarSteps = itemAmount - maxShownItems;
		public int scrollBarBarHeight = height / (scrollBarSteps + 1);
		public int scrollBarBarY = currentStep * scrollBarBarHeight;
	}
}
