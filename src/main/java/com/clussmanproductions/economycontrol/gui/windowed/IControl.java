package com.clussmanproductions.economycontrol.gui.windowed;

public interface IControl {
	void draw(int mouseX, int mouseY, float partialTicks);
	default boolean mouseClick(int mouseX, int mouseY, int mouseButton) { return false; }
	default boolean keyTyped(char keyedChar, int typedKey) { return false; }
	default void mouseClickMoved(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) { }
	default void mouseReleased(int mouseX, int mouseY, int state) { }
}
