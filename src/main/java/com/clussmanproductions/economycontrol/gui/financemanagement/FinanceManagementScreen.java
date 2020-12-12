package com.clussmanproductions.economycontrol.gui.financemanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.clussmanproductions.economycontrol.gui.GuiUtilities;
import com.clussmanproductions.economycontrol.gui.financemanagement.screens.gov.SelectGovernment;
import com.clussmanproductions.economycontrol.gui.windowed.Window;
import com.clussmanproductions.economycontrol.gui.windowed.Window.WindowEvents;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.net.financemanagement.GetFinanceContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class FinanceManagementScreen extends GuiScreen {
	private GuiButtonExt personalFinance;
	private GuiButtonExt companyFinance;
	private GuiButtonExt governmentFinance;
	private boolean loadingMainScreen = true;
	private LinkedHashMap<UUID, Window> windowStack = new LinkedHashMap<>();
	private LinkedList<Window> taskWindows = new LinkedList<>();
	private Window activeWindow;
	int horizontalCenter = width / 2;
	int verticalCenter = height / 2;
	
	@Override
	public void initGui() {
		
		super.initGui();
		
		horizontalCenter = width / 2;
		verticalCenter = height / 2;
		
		loadingMainScreen = true;
		
		personalFinance = new GuiButtonExt(COMPONENTIDS.personal, 0, height - 20, 80, 20, "Personal");
		personalFinance.visible = false;
		companyFinance = new GuiButtonExt(COMPONENTIDS.company, 80 + 4, height - 20, 80, 20, "Company");
		companyFinance.visible = false;
		governmentFinance = new GuiButtonExt(COMPONENTIDS.government, 80 + 4 + 80 + 4, height - 20, 80, 20, "Government");
		governmentFinance.visible = false;
		
		buttonList.add(personalFinance);
		buttonList.add(companyFinance);
		buttonList.add(governmentFinance);
		
		GetFinanceContext get = new GetFinanceContext();
		PacketHandler.INSTANCE.sendToServer(get);
	}
	
	public void setFinanceManagementScreenContext(FinanceManagementScreenContext context)
	{
		personalFinance.visible = true;
		companyFinance.visible = true;
		governmentFinance.visible = true;
		
		companyFinance.enabled = context.canManageCompanies() || context.canCreateCompanies();
		governmentFinance.enabled = context.canManageGovernments() || context.canCreateGovernments();
		
		loadingMainScreen = false;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		int stringWidth = fontRenderer.getStringWidth("Finance Management");
		fontRenderer.drawStringWithShadow("Finance Management", horizontalCenter - (stringWidth / 2), 10, 0xFFFFFF);
		
		if (loadingMainScreen)
		{
			stringWidth = fontRenderer.getStringWidth("Retrieving information");
			fontRenderer.drawString("Retrieving information", horizontalCenter - (stringWidth / 2), verticalCenter - (fontRenderer.FONT_HEIGHT / 2), 0xFFFFFF);
			return;
		}
		
		drawWindows(mouseX, mouseY, partialTicks);
		drawTaskBar(mouseX, mouseY);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void drawTaskBar(int mouseX, int mouseY)
	{
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.disableTexture2D();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		
		// Task bar and separator
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(width, height - 20, zLevel).color(0F, 0F, 0.5F, 1F).endVertex();
		buffer.pos(0, height - 20, zLevel).color(0F, 0F, 0.5F, 1F).endVertex();
		buffer.pos(0, height, zLevel).color(0F, 0F, 0.5F, 1F).endVertex();
		buffer.pos(width, height, zLevel).color(0F, 0F, 0.5F, 1F).endVertex();
		
		buffer.pos(252, height - 20, zLevel).color(0F, 0F, 0.25F, 1F).endVertex();
		buffer.pos(250, height - 20, zLevel).color(0F, 0F, 0.25F, 1F).endVertex();
		buffer.pos(250, height, zLevel).color(0F, 0F, 0.25F, 1F).endVertex();
		buffer.pos(252, height, zLevel).color(0F, 0F, 0.25F, 1F).endVertex();
		
		// Selected and hover tasks
		if (activeWindow != null)
		{
			int activeWindowIndex = taskWindows.indexOf(activeWindow);
			int activeWindowX = 52 * activeWindowIndex + 254;
			
			buffer.pos(activeWindowX + 50, height - 18, zLevel).color(128, 128, 128, 255).endVertex();
			buffer.pos(activeWindowX, height - 18, zLevel).color(128, 128, 128, 255).endVertex();
			buffer.pos(activeWindowX, height - 2, zLevel).color(128, 128, 128, 255).endVertex();
			buffer.pos(activeWindowX + 50, height - 2, zLevel).color(128, 128, 128, 255).endVertex();
		}

		int maxTasks = (width - 254 - 20) / 52;
		if (mouseY > height - 18 && mouseY < height - 2 && mouseX >= 254)
		{
			int hoverTask = (mouseX - 254) / 52;
			
			if (hoverTask < taskWindows.size())
			{
				int hoverTaskX = hoverTask * 52 + 254;
				
				buffer.pos(hoverTaskX + 50, height - 18, zLevel).color(103, 103, 103, 255).endVertex();
				buffer.pos(hoverTaskX, height - 18, zLevel).color(103, 103, 103, 255).endVertex();
				buffer.pos(hoverTaskX, height - 2, zLevel).color(103, 103, 103, 255).endVertex();
				buffer.pos(hoverTaskX + 50, height - 2, zLevel).color(103, 103, 103, 255).endVertex();
			}
		}
		
		tess.draw();

		// Task borders
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		int lastX = 254;
		for(Window window : taskWindows)
		{			
			buffer.pos(lastX + 50, height - 18, zLevel).color(1, 1, 1, 1F).endVertex();
			buffer.pos(lastX, height - 18, zLevel).color(1, 1, 1, 1F).endVertex();
			
			buffer.pos(lastX, height - 18, zLevel).color(1, 1, 1, 1F).endVertex();
			buffer.pos(lastX, height - 2, zLevel).color(1, 1, 1, 1F).endVertex();

			buffer.pos(lastX, height - 2, zLevel).color(1, 1, 1, 1F).endVertex();			
			buffer.pos(lastX + 50, height - 2, zLevel).color(1, 1, 1, 1F).endVertex();
			
			buffer.pos(lastX + 50, height - 2, zLevel).color(1, 1, 1, 1F).endVertex();
			buffer.pos(lastX + 50, height - 18, zLevel).color(1, 1, 1, 1F).endVertex();
			
			lastX += 52;
		}
		
		tess.draw();
		
		GlStateManager.enableTexture2D();
		
		lastX = 254;
		for(Window window : taskWindows)
		{
			String windowText = window.getText();
			if (fontRenderer.getStringWidth(windowText) > 48)
			{
				windowText = fontRenderer.trimStringToWidth(windowText, 48);
				if (windowText.length() > 3)
				{
					windowText = windowText.substring(0, windowText.length() - 3) + "...";
				}
				else
				{
					windowText = "...";
				}
			}
			
			fontRenderer.drawString(windowText, lastX + 2, height - 14, 0xFFFFFF);
			lastX += 52;
		}
	}
	
	private void drawWindows(int mouseX, int mouseY, float partialTicks)
	{
		for(Window window : windowStack.values())
		{
			window.draw(mouseX, mouseY, partialTicks);
		}
	}
	
	public Window createWindow(int width, int height)
	{
		Window newWindow = new Window(this, fontRenderer, horizontalCenter - (width / 2), verticalCenter - (height / 2), width, height);
		newWindow.addListener(WindowEvents.Close, (window) -> closeWindow(window));
		newWindow.addListener(WindowEvents.Minimize, window -> minimizeWindow(window));
		windowStack.put(newWindow.getWindowID(), newWindow);
		taskWindows.add(newWindow);
		return newWindow;
	}
	
	public void closeWindow(Window window)
	{
		windowStack.remove(window.getWindowID());
		taskWindows.remove(window);
		
		if (activeWindow == window)
		{
			activeWindow = null;
		}
	}
	
	public void minimizeWindow(Window window)
	{
		window.setHidden(true);
		
		if (activeWindow == window)
		{
			activeWindow = null;
			window.setActive(false);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		boolean clickIntercepted = false;
		ArrayList<Window> windows = new ArrayList<>(windowStack.values());
		Collections.reverse(windows);
		
		for(Window window : windows)
		{
			if (!clickIntercepted && !window.isHidden() && GuiUtilities.isMouseInRegion(window.getX(), window.getY(), window.getX() + window.getWidth(), window.getY() + window.getHeight() - 19, mouseX, mouseY))
			{
				clickIntercepted = true;
				activeWindow = window;
				activeWindow.setActive(true);
				activeWindow.mouseClick(mouseX, mouseY, mouseButton);
			}
			else
			{
				window.setActive(false);
			}
		}
		
		if (!clickIntercepted && GuiUtilities.isMouseInRegion(254, height - 18, width - 20, height - 2, mouseX, mouseY))
		{
			int maxTasks = (width - 254 - 20) / 52;
			int taskIndex = (mouseX - 254) / 52;
			
			if (taskIndex < taskWindows.size())
			{
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
				clickIntercepted = true;
				activeWindow = taskWindows.get(taskIndex);
				activeWindow.setHidden(false);
				activeWindow.setActive(true);
			}
		}
		
		if (!clickIntercepted)
		{
			activeWindow = null;
		}
		else if (activeWindow != null)
		{
			windowStack.remove(activeWindow.getWindowID());
			windowStack.put(activeWindow.getWindowID(), activeWindow);
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		
		if (activeWindow != null)
		{
			activeWindow.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		
		if (activeWindow != null)
		{
			activeWindow.mouseReleased(mouseX, mouseY, state);
		}
	}
		
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		if (activeWindow != null)
		{
			activeWindow.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch(button.id)
		{
			case COMPONENTIDS.government:
				Window governmentWindow = createWindow(200, 200);
				governmentWindow.setText("Select Government");
				governmentWindow.generateScreen(SelectGovernment.class);
				break;
		}
	}
			
	private static class COMPONENTIDS
	{
		public final static int personal = 0;
		public final static int company = 1;
		public final static int government = 2;
	}
}
