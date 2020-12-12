package com.clussmanproductions.economycontrol.gui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.gui.windowed.ComboBox;
import com.clussmanproductions.economycontrol.gui.windowed.ComboBox.ComboBoxItem;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.net.taggingstation.ValueUpdated;
import com.clussmanproductions.economycontrol.tile.SecurityTaggingStationTileEntity;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiSecurityTaggingStation extends GuiContainer {
	
	private SecurityTaggingStationTileEntity te;
	public static final int WIDTH = 176;
	public static final int HEIGHT = 166;
	GuiTextField unitCost;
	GuiTextField tagCost;
	ComboBox companySelector;
	
	private static final ResourceLocation background = new ResourceLocation(EconomyControl.MODID, "textures/gui/security_tagging_station.png");

	public GuiSecurityTaggingStation(SecurityTaggingStationTileEntity te, ContainerSecurityTaggingStation inventorySlotsIn) {
		super(inventorySlotsIn);
		inventorySlotsIn.onItemInserted = () -> onItemInserted();
		
		this.te = te;
		
		xSize = WIDTH;
		ySize = HEIGHT;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		int stringWidth = fontRenderer.getStringWidth("Company: ");
		companySelector = new ComboBox(fontRenderer, guiLeft + stringWidth + 4, guiTop + 15, xSize - stringWidth - 4 - 4, 10);
		companySelector.addComboBoxItem(new ComboBoxItem("RZBC - Iron River", UUID.fromString("e846e9ca-dee5-4fef-a82d-d0363b43d306")));
		companySelector.addComboBoxItem(new ComboBoxItem("RZBC - Direct", UUID.fromString("7b2dfdcf-3166-4a8b-bc09-8732cc5d2b78")));
		companySelector.addComboBoxItem(new ComboBoxItem("RZBC - Autumn Valley", UUID.fromString("b3bf333b-5184-4acc-a961-3a5a26b399f3")));
		companySelector.addComboBoxItem(new ComboBoxItem("RZBC - Sodor City", UUID.fromString("e78be427-9c35-4390-968d-8fd4d5ef16c6")));
		companySelector.addComboBoxItem(new ComboBoxItem("RZBC - Great River", UUID.fromString("0f33ce05-6956-4fbe-a6d1-4b41ee134247")));
		
		unitCost = new GuiTextField(0, fontRenderer, guiLeft + 32, guiTop + 59, 40, 20);
		tagCost = new GuiTextField(1, fontRenderer, guiLeft + xSize - 40 - 6, guiTop + 59, 40, 20);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		ComboBoxItem preClickItem = companySelector.getSelectedItem();
		companySelector.mouseClick(mouseX, mouseY, mouseButton);
		boolean sendUpdate = preClickItem != companySelector.getSelectedItem();
		
		unitCost.mouseClicked(mouseX, mouseY, mouseButton);
		tagCost.mouseClicked(mouseX, mouseY, mouseButton);
		
		if (sendUpdate)
		{
			sendUpdate();
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		companySelector.mouseClickMoved(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		companySelector.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		companySelector.handleMouseInput();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		if (unitCost.isFocused() || tagCost.isFocused())
		{
			if (Character.isDigit(typedChar))
			{
				String text = unitCost.isFocused() ? unitCost.getText() : tagCost.getText();
				text += Character.toString(typedChar);
				
				boolean isParseValid = false;
				try
				{
					Double.parseDouble(text);
					isParseValid = true;
				}
				catch(NumberFormatException ex)
				{
					return;
				}
				
				if (isParseValid && (!text.contains(".") || text.substring(text.lastIndexOf(".") + 1).length() <= 2))
				{
					unitCost.textboxKeyTyped(typedChar, keyCode);
					tagCost.textboxKeyTyped(typedChar, keyCode);					
				}				
			}
			
			if (Character.toString(typedChar).equals("."))
			{
				String text = unitCost.isFocused() ? unitCost.getText() : tagCost.getText();
				
				if (!text.contains("."))
				{
					unitCost.textboxKeyTyped(typedChar, keyCode);
					tagCost.textboxKeyTyped(typedChar, keyCode);
				}
			}
			
			if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE)
			{
				unitCost.textboxKeyTyped(typedChar, keyCode);
				tagCost.textboxKeyTyped(typedChar, keyCode);
			}
			
			ItemStack stackTagging = inventorySlots.getSlot(0).getStack();
			if (stackTagging != null && stackTagging.getItem() != Items.AIR)
			{
				if (unitCost.isFocused())
				{
					boolean unitCostIsValid = false;
					long unitCostAmount = Long.MIN_VALUE;
					try
					{
						double unitCostParsed = Double.parseDouble(unitCost.getText());
						unitCostAmount = (long)(unitCostParsed * 100D);
					}
					catch(NumberFormatException ex){}
					
					if (unitCostAmount != Long.MIN_VALUE)
					{
						unitCostAmount *= stackTagging.getCount();
					}
					
					setTagCostText(unitCostAmount);
				}
				
				if (tagCost.isFocused())
				{
					long tagCostAmount = Long.MIN_VALUE;
					try
					{
						double tagCostParsed = Double.parseDouble(tagCost.getText());
						tagCostAmount = (long)(tagCostParsed * 100D);
					}
					catch(NumberFormatException ex) {}
					
					if (tagCostAmount != Long.MIN_VALUE)
					{
						tagCostAmount /= stackTagging.getCount();
					}
					setUnitCostText(tagCostAmount);
				}
			}
			
			if (keyCode != Keyboard.KEY_ESCAPE)
			{
				sendUpdate();
			}
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		
		fontRenderer.drawString("Security Tagging Station", guiLeft + 4, guiTop + 4, 0x8b8b8b);
		fontRenderer.drawString("Company: ", guiLeft + 4, guiTop + 16, 0x8b8b8b);
		fontRenderer.drawString("Unit", guiLeft + 4, guiTop + 60, 0x8b8b8b);
		fontRenderer.drawString("Cost:", guiLeft + 4, guiTop + 70, 0x8b8b8b);
		fontRenderer.drawString("Tag", guiLeft + 103, guiTop + 60, 0x8b8b8b);
		fontRenderer.drawString("Cost:", guiLeft + 103, guiTop + 70, 0x8b8b8b);
		companySelector.draw(mouseX, mouseY, partialTicks);
		
		unitCost.drawTextBox();
		tagCost.drawTextBox();
	}
	
	@Override
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
		// TODO Auto-generated method stub
		if (companySelector.isOpen())
		{
			return false;
		}
		return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		
		
	}

	private void sendUpdate()
	{
		ValueUpdated updated = new ValueUpdated();
		updated.tePos = te.getPos();
		
		try
		{
			double tagCostDouble = Double.parseDouble(tagCost.getText());
			updated.tagCost = Optional.of((long)(tagCostDouble * 100D));
		}
		catch(NumberFormatException ex){}
		
		ComboBoxItem selectedCompany = companySelector.getSelectedItem();
		if (selectedCompany != null)
		{
			updated.company = (UUID)selectedCompany.getValue();
		}
		
		PacketHandler.INSTANCE.sendToServer(updated);
	}

	private void onItemInserted()
	{
		ItemStack stack = inventorySlots.getSlot(0).getStack();
		if (stack.getItem() == Items.AIR)
		{
			return;
		}
		
		long unitCost = Long.MIN_VALUE;
		try
		{
			double tagCostParsed = Double.parseDouble(tagCost.getText());
			unitCost = (long)(tagCostParsed * 100D);
		}
		catch(NumberFormatException ex) {}
		
		if (unitCost != Long.MIN_VALUE)
		{
			unitCost /= stack.getCount();
		}
		
		setUnitCostText(unitCost);
		sendUpdate();
	}
	
	private void setUnitCostText(long amount)
	{
		if (amount == Long.MIN_VALUE)
		{
			unitCost.setText("");
			return;
		}
		setUnitCostText((double)amount / 100D);
	}
	
	private void setTagCostText(long amount)
	{
		if (amount == Long.MIN_VALUE)
		{
			tagCost.setText("");
			return;
		}
		setTagCostText((double)amount / 100D);
	}
	
	private void setUnitCostText(double amount)
	{
		setCostText(amount, unitCost);
	}
	
	private void setTagCostText(double amount)
	{
		setCostText(amount, tagCost);
	}
	
	private void setCostText(double amount, GuiTextField textField)
	{
		String amountToSet = Double.toString(amount);
		if (!amountToSet.contains("."))
		{
			amountToSet += ".00";
		}
		
		String pennies = amountToSet.substring(amountToSet.lastIndexOf(".") + 1);
		if (pennies.length() == 1)
		{
			amountToSet += "0";
		}
		else if (pennies.length() > 2)
		{
			amountToSet = amountToSet.substring(0, amountToSet.lastIndexOf(".") + 3);
		}
		
		textField.setText(amountToSet);
	}
}
