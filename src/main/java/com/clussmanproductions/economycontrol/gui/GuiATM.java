package com.clussmanproductions.economycontrol.gui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.clussmanproductions.economycontrol.EconomyControl;
import com.clussmanproductions.economycontrol.ModItems;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountData;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountHistoryData.History;
import com.clussmanproductions.economycontrol.data.bankaccount.BankAccountTypes;
import com.clussmanproductions.economycontrol.item.ItemMoney.MoneyTypes;
import com.clussmanproductions.economycontrol.net.PacketHandler;
import com.clussmanproductions.economycontrol.net.atm.AddBankAccount;
import com.clussmanproductions.economycontrol.net.atm.DepositAmount;
import com.clussmanproductions.economycontrol.net.atm.Initialize;
import com.clussmanproductions.economycontrol.net.atm.InitializeRetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.RetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.RetrieveHistories;
import com.clussmanproductions.economycontrol.net.atm.TransferAmount;
import com.clussmanproductions.economycontrol.net.atm.TransferRetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.Withdrawal;
import com.clussmanproductions.economycontrol.tile.ATMTileEntity;
import com.google.common.collect.ImmutableMap;
import com.ibm.icu.util.Calendar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiATM extends GuiScreen {
	EntityPlayer player;
	ATMTileEntity atm;
	private final int texWidth = 300;
	private final int texHeight = 200;
	
	int horizontalCenter;
	int verticalCenter;
	int halfTexWidth;
	int halfTexHeight;
	int texTop;
	int texLeft;
	List<GuiTextField> textFields = new ArrayList<>();
	String headerText = "";
	
	private Screens previousScreen;
	public Screens currentScreen;
	public final ImmutableMap<Screens, IScreenData> screenDataForScreen; 
	
	public GuiATM(EntityPlayer player, ATMTileEntity atm)
	{
		this.player = player;
		this.atm = atm;
		screenDataForScreen = ImmutableMap
				.<Screens, IScreenData>builder()
				.put(Screens.Initialize, new InitializeData())
				.put(Screens.Loading, new LoadingData())
				.put(Screens.BankAccountSelection, new BankAccountSelectionData())
				.put(Screens.Error, new ErrorData())
				.put(Screens.NewAccount, new NewAccountData())
				.put(Screens.AccountMenu, new AccountMenuData())
				.put(Screens.Withdraw, new WithdrawalData())
				.put(Screens.Success, new SuccessData())
				.put(Screens.TransferAccountSelection, new TransferAccountSelectionData())
				.put(Screens.TransferAmount, new TransferAmountData())
				.put(Screens.Deposit, new DepositData())
				.put(Screens.InitializeFeeAccount, new InitializeFeeAccountData())
				.put(Screens.History, new HistoryData())
				.build();
	}
	
	private boolean isFirstTick = true;
	@Override
	public void initGui() {
		super.initGui();
		
		LoadingData loadingData = (LoadingData)screenDataForScreen.get(Screens.Loading);
		loadingData.message = "Connecting to host...";

		previousScreen = Screens.Loading;
		currentScreen = Screens.Loading;
		
		horizontalCenter = width / 2;
		verticalCenter = height / 2;
		halfTexWidth = texWidth / 2;
		halfTexHeight = texHeight / 2;
		texTop = verticalCenter - halfTexHeight;
		texLeft = horizontalCenter - halfTexWidth;
		
		isFirstTick = true;
		
		for(IScreenData data : screenDataForScreen.values())
		{
			data.init();
		}
		
		for(GuiButton button : buttonList)
		{
			button.visible = false;
		}
		
		for(GuiTextField field : textFields)
		{
			field.setVisible(false);
		}
		
		if (!atm.getIsInitialized() && !atm.getOwner().equals(player.getName()))
		{
			isFirstTick = false;
			
			ErrorData errorData = (ErrorData)screenDataForScreen.get(Screens.Error);
			errorData.errorMessage = "This ATM has not been setup yet";
			currentScreen = Screens.Error;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (isFirstTick)
		{
			screenDataForScreen.get(currentScreen).onScreenChangedTo();
			if (atm.getIsInitialized())
			{
				PacketHandler.INSTANCE.sendToServer(new RetrieveBankAccounts());
			}
			else
			{
				Initialize initialize = new Initialize();
				initialize.pos = atm.getPos();
				PacketHandler.INSTANCE.sendToServer(initialize);
			}
			
			isFirstTick = false;
		}
		
		if (!previousScreen.equals(currentScreen))
		{
			for(GuiButton button : buttonList)
			{
				button.visible = false;
			}
			
			for(GuiTextField field : textFields)
			{
				field.setVisible(false);
			}

			screenDataForScreen.get(currentScreen).onScreenChangedTo();
			previousScreen = currentScreen;
		}
		
		drawDefaultBackground();
		
		mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/atm.png"));
		
		drawModalRectWithCustomSizedTexture(horizontalCenter - halfTexWidth, verticalCenter - halfTexHeight, 0, 0, texWidth, texHeight, texWidth, texHeight);
		
		int fontWidth = fontRenderer.getStringWidth(player.getName());
		fontRenderer.drawStringWithShadow(player.getName(), texLeft + texWidth - 10 - fontWidth, texTop + 10, 0xFFFF00);
		
		fontWidth = fontRenderer.getStringWidth("\u00a7l" + headerText);
		fontRenderer.drawStringWithShadow("\u00a7l" + headerText, horizontalCenter - (fontWidth / 2), texTop + 30, 0xADD8E6);
		
		screenDataForScreen.get(currentScreen).draw(mouseX, mouseY);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		screenDataForScreen.get(currentScreen).buttonClick(button);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		screenDataForScreen.get(currentScreen).mouseClick(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		screenDataForScreen.get(currentScreen).keyTyped(typedChar, keyCode);
	}
	
	public enum Screens
	{
		Initialize,
		Loading,
		BankAccountSelection,
		Error,
		NewAccount,
		AccountMenu,
		Withdraw,
		Success,
		TransferAccountSelection,
		TransferAmount,
		Deposit,
		InitializeFeeAccount,
		History
	}
	
	interface IScreenData
	{
		void init();
		void onScreenChangedTo();
		void draw(int mouseX, int mouseY);
		default void buttonClick(GuiButton button) { }
		default void mouseClick(int mouseX, int mouseY, int mouseButton) { }
		default void keyTyped(char typedChar, int keyCode) { }
	}
	
	public class InitializeData implements IScreenData
	{
		public BankAccountData selectedAccount;
		
		GuiTextField name;
		GuiTextField withdrawalFee;
		GuiTextField selectedBank;
		GuiButtonExt selectBank;
		GuiButtonExt initialize;
		@Override
		public void init() {
			name = new GuiTextField(0, fontRenderer, horizontalCenter - 50, verticalCenter - 50, 100, 20);
			withdrawalFee = new GuiTextField(1, fontRenderer, horizontalCenter - 50, verticalCenter - 25, 100, 20);
			selectedBank = new GuiTextField(2, fontRenderer, horizontalCenter - 50, verticalCenter, 100, 20);
			selectBank = new GuiButtonExt(3, horizontalCenter + 55, verticalCenter, 20, 20, "...");
			initialize = new GuiButtonExt(4, horizontalCenter - 50, verticalCenter + 25, 100, 20, "Initialize");
			
			textFields.add(name);
			textFields.add(withdrawalFee);
			textFields.add(selectedBank);
			buttonList.add(selectBank);
			buttonList.add(initialize);
		}

		@Override
		public void onScreenChangedTo() {
			headerText = "Initialize ATM";
			
			name.setText(atm.getName());
			withdrawalFee.setText(String.valueOf(atm.getWithdrawalFeeAmount() / 100D));
			
			name.setVisible(true);
			withdrawalFee.setVisible(true);
			selectedBank.setVisible(true);
			selectedBank.setEnabled(false);
			selectedBank.setText(String.format("%s (%s)", selectedAccount.getBankAccountName(), selectedAccount.getBankAccountType().getFriendlyName()));
			selectedBank.setCursorPositionZero();
			selectBank.visible=true;
			initialize.visible=true;
			
			boolean initializeEnabled = !name.getText().equals("");
			if (initializeEnabled)
			{
				try
				{
					Double.parseDouble(withdrawalFee.getText());
				}
				catch(NumberFormatException ex)
				{
					initializeEnabled = false;
				}
			}
			
			initialize.enabled=initializeEnabled;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			name.drawTextBox();
			withdrawalFee.drawTextBox();
			selectedBank.drawTextBox();
			
			int fontWidth = fontRenderer.getStringWidth("Name:");
			fontRenderer.drawString("Name:", horizontalCenter - 50 - fontWidth - 5, verticalCenter - 45, 0xFFFFFF);
			
			fontWidth = fontRenderer.getStringWidth("Withdrawal Fee:");
			fontRenderer.drawString("Withdrawal Fee:", horizontalCenter - 50 - fontWidth - 5, verticalCenter - 20, 0xFFFFFF);
			
			fontWidth = fontRenderer.getStringWidth("Fee Account:");
			fontRenderer.drawString("Fee Account:", horizontalCenter - 50 - fontWidth - 5, verticalCenter + 5, 0xFFFFFF);
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			double withdrawalFeeAmount = 0;
			switch(button.id)
			{
				case 3:
					try
					{
						withdrawalFeeAmount = Double.parseDouble(withdrawalFee.getText());
					}
					catch(Exception ex) { }
					
					atm.setName(name.getText());
					atm.setWithdrawalFeeAmount(Math.round(withdrawalFeeAmount * 100));
					atm.syncWithServer();
					
					currentScreen = Screens.Loading;
					PacketHandler.INSTANCE.sendToServer(new InitializeRetrieveBankAccounts());
					break;
				case 4:
					try
					{
						withdrawalFeeAmount = Double.parseDouble(withdrawalFee.getText());
					}
					catch(Exception ex) { return; }
					
					atm.setName(name.getText());
					atm.setWithdrawalFeeAmount(Math.round(withdrawalFeeAmount * 100));
					atm.setIsInitialized(true);
					atm.syncWithServer();
					
					currentScreen = Screens.Loading;
					RetrieveBankAccounts retrieve = new RetrieveBankAccounts();
					PacketHandler.INSTANCE.sendToServer(retrieve);
					break;
			}
		}
			
		
		@Override
		public void keyTyped(char typedChar, int keyCode) {
			boolean nameValid = false;
			boolean withdrawalFeeValid = false;
			
			name.textboxKeyTyped(typedChar, keyCode);
			withdrawalFee.textboxKeyTyped(typedChar, keyCode);
			
			nameValid = name.getText() != null && !name.getText().equals("");
			try
			{
				String withdrawalFeeText = withdrawalFee.getText();
				Double.parseDouble(withdrawalFeeText);
				
				int decimals = -1;
				if (withdrawalFeeText.contains("."))
				{
					withdrawalFeeText = withdrawalFeeText.substring(withdrawalFeeText.indexOf('.') + 1);
					decimals = withdrawalFeeText.length();
				}
				
				withdrawalFeeValid = decimals != 0 && decimals <= 2;
			}
			catch(NumberFormatException ex)
			{
				withdrawalFeeValid = false;
			}
			
			initialize.enabled = nameValid && withdrawalFeeValid;
		}
		
		@Override
		public void mouseClick(int mouseX, int mouseY, int mouseButton) {
			name.mouseClicked(mouseX, mouseY, mouseButton);
			withdrawalFee.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	
	class LoadingData implements IScreenData
	{
		public String message;
		private int frameCounter = 0;
		
		public void draw(int mouseX, int mouseY)
		{
			mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/signal.png"));
			drawModalRectWithCustomSizedTexture(horizontalCenter - 16, verticalCenter - 16, 0, (frameCounter / 20) * 32, 32, 32, 32, 128);
			
			if (++frameCounter >= 80)
			{
				frameCounter = 0;
			}
			
			int textWidth = fontRenderer.getStringWidth(message);
			fontRenderer.drawString(message, horizontalCenter - (textWidth / 2), verticalCenter + 20, 0xFFFFFF);
		}

		@Override
		public void init() { }

		@Override
		public void onScreenChangedTo() { }
	}
	
	public class BankAccountSelectionData implements IScreenData
	{
		public List<BankAccountData> bankAccounts;
		public int skip;
		final int maxButtonsPerScreen = 4;
		GuiButtonExt firstAccount;
		GuiButtonExt secondAccount;
		GuiButtonExt thirdAccount;
		GuiButtonExt newAccount;
		GuiButtonExt prevPage;
		GuiButtonExt nextPage;
		GuiButtonExt settings;
		
		@Override
		public void draw(int mouseX, int mouseY) { }

		@Override
		public void init() {
			firstAccount = new GuiButtonExt(0, horizontalCenter - 50, verticalCenter - 55, 100, 20, "");
			secondAccount = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter - 25, 100, 20, "");
			thirdAccount = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter + 5, 100, 20, "");
			newAccount = new GuiButtonExt(3, horizontalCenter - 50, verticalCenter + 35, 100, 20, "");
			newAccount.displayString = "+ New Account";
			prevPage = new GuiButtonExt(4, horizontalCenter - 50, verticalCenter + 65, 20, 20, "<");
			nextPage = new GuiButtonExt(5, horizontalCenter + 30, verticalCenter + 65, 20, 20, ">");
			settings = new GuiButtonExt(6, texLeft + 5, texTop + texHeight - 25, 50, 20, "Settings");
			
			buttonList.add(firstAccount);
			buttonList.add(secondAccount);
			buttonList.add(thirdAccount);
			buttonList.add(newAccount);
			buttonList.add(prevPage);
			buttonList.add(nextPage);
			buttonList.add(settings);
		}

		@Override
		public void onScreenChangedTo() {
			headerText = "Select Bank Account";
			
			firstAccount.visible = false;
			secondAccount.visible = false;
			thirdAccount.visible = false;			
			newAccount.visible = true;
			nextPage.visible = true;
			nextPage.enabled = bankAccounts != null && bankAccounts.size() > skip + maxButtonsPerScreen - 1;
			prevPage.visible = true;
			prevPage.enabled = skip != 0;
			settings.visible = atm.getOwner().equals(player.getName());
			
			if (bankAccounts != null)
			{
				for(int i = skip; i < skip + maxButtonsPerScreen - 1; i++)
				{
					if (i >= bankAccounts.size())
					{
						break;
					}
					
					BankAccountData bankAccount = bankAccounts.get(i);
					switch(i % 3)
					{
						case 0:
							firstAccount.displayString = String.format("%s (%s)", bankAccount.getBankAccountName(), bankAccount.getBankAccountType().getFriendlyName());
							firstAccount.visible = true;
							break;
						case 1:
							secondAccount.displayString = String.format("%s (%s)", bankAccount.getBankAccountName(), bankAccount.getBankAccountType().getFriendlyName());
							secondAccount.visible = true;
							break;
						case 2:
							thirdAccount.displayString = String.format("%s (%s)", bankAccount.getBankAccountName(), bankAccount.getBankAccountType().getFriendlyName());
							thirdAccount.visible = true;
							break;
					}
				}
			}
		}

		@Override
		public void buttonClick(GuiButton button)
		{
			switch(button.id)
			{
				case 0:
					BankAccountData bankAccount = bankAccounts.get(skip);
					AccountMenuData data1 = (AccountMenuData)screenDataForScreen.get(Screens.AccountMenu);
					data1.bankAccount = bankAccount;
					data1.canTransfer = bankAccounts.size() > 1;
					currentScreen = Screens.AccountMenu;
					break;
				case 1:
					BankAccountData bankAccount2 = bankAccounts.get(skip + 1);
					AccountMenuData data2 = (AccountMenuData)screenDataForScreen.get(Screens.AccountMenu);
					data2.bankAccount = bankAccount2;
					data2.canTransfer = bankAccounts.size() > 1;
					currentScreen = Screens.AccountMenu;					
					break;
				case 2:
					BankAccountData bankAccount3 = bankAccounts.get(skip + 2);
					AccountMenuData data3 = (AccountMenuData)screenDataForScreen.get(Screens.AccountMenu);
					data3.bankAccount = bankAccount3;
					data3.canTransfer = bankAccounts.size() > 1;
					currentScreen = Screens.AccountMenu;
					break;
				case 3:
					currentScreen = Screens.NewAccount;
					break;
				case 4:
					if (bankAccounts == null)
					{
						prevPage.enabled = false;
						return;
					}
					
					skip -= 3;
					if (skip < 0)
					{
						skip = 0;
					}
					
					prevPage.enabled = skip == 0;
					onScreenChangedTo();
					break;
				case 5:
					if (bankAccounts == null)
					{
						nextPage.enabled = false;
						return;
					}
					
					if (skip + (maxButtonsPerScreen - 1) < bankAccounts.size())
					{
						skip += maxButtonsPerScreen - 1;
					}
					
					nextPage.enabled = skip + (maxButtonsPerScreen - 1) < bankAccounts.size();
					onScreenChangedTo();
					break;
				case 6:
					Initialize init = new Initialize();
					init.pos = atm.getPos();
					currentScreen = Screens.Loading;
					PacketHandler.INSTANCE.sendToServer(init);
					break;
			}
		}
	}
	
	public class ErrorData implements IScreenData
	{
		public String errorMessage;
		
		@Override
		public void draw(int mouseX, int mouseY)
		{
			mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation(EconomyControl.MODID, "textures/gui/cancel.png"));
			GlStateManager.color(204F / 256F, 0, 0);
			drawModalRectWithCustomSizedTexture(horizontalCenter - 16, verticalCenter - 16, 0, 0, 32, 32, 32, 32);
			
			int textWidth = fontRenderer.getStringWidth("Internal Server Error");
			fontRenderer.drawString("Internal Server Error", horizontalCenter - (textWidth / 2), verticalCenter - 30, 0xCC0000);
			
			textWidth = fontRenderer.getStringWidth(errorMessage);
			fontRenderer.drawString(errorMessage, horizontalCenter - (textWidth / 2), verticalCenter + 20, 0xFFFFFF);
		}

		@Override
		public void init() { }

		@Override
		public void onScreenChangedTo() { }
	}

	public class NewAccountData implements IScreenData
	{
		GuiCheckBox checking;
		GuiCheckBox savings;
		GuiTextField name;
		GuiButtonExt cancel;
		GuiButtonExt save;
		
		@Override
		public void init() {
			name = new GuiTextField(2, fontRenderer, horizontalCenter - 50, verticalCenter - 10, 100, 20);
			
			checking = new GuiCheckBox(0, name.x, verticalCenter - 26, "Checking", true);
			savings = new GuiCheckBox(1, checking.x + checking.width + 5, verticalCenter - 26, "Savings", false);
			cancel = new GuiButtonExt(3, name.x, verticalCenter + 15, 40, 20, "Cancel");
			save = new GuiButtonExt(4, name.x + name.width - 40, verticalCenter + 15, 40, 20, "Save");
			
			buttonList.add(checking);
			buttonList.add(savings);
			buttonList.add(cancel);
			buttonList.add(save);
			textFields.add(name);
		}

		@Override
		public void onScreenChangedTo() {
			headerText = "New Bank Account";
			
			checking.visible = true;
			checking.setIsChecked(true);
			
			savings.visible = true;
			savings.setIsChecked(false);
			
			name.setVisible(true);
			name.setText("");
			
			save.visible = true;
			save.enabled = false;
			
			cancel.visible = true;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			name.drawTextBox();
			
			int textWidth = fontRenderer.getStringWidth("Type:");
			fontRenderer.drawString("Type:", horizontalCenter - 55 - textWidth, verticalCenter - 25, 0xFFFFFF);
			
			textWidth = fontRenderer.getStringWidth("Name:");
			fontRenderer.drawString("Name:", horizontalCenter - 55 - textWidth, verticalCenter - 4, 0xFFFFFF);
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 0:
					savings.setIsChecked(false);
					checking.setIsChecked(true);
					break;
				case 1:
					checking.setIsChecked(false);
					savings.setIsChecked(true);
					break;
				case 3:
					currentScreen = Screens.BankAccountSelection;
					break;
				case 4:
					BankAccountData data = new BankAccountData(null);
					data.setBankAccountName(name.getText());
					data.setBankAccountType(checking.isChecked() ? BankAccountTypes.PersonalChecking : BankAccountTypes.PersonalSavings);
					
					AddBankAccount packet = new AddBankAccount();
					packet.bankAccount = data;
					PacketHandler.INSTANCE.sendToServer(packet);
					
					PacketHandler.INSTANCE.sendToServer(new RetrieveBankAccounts());
					
					currentScreen = Screens.Loading;
					break;
			}
		}
		
		@Override
		public void mouseClick(int mouseX, int mouseY, int mouseButton) {
			name.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		public void keyTyped(char typedChar, int keyCode) {
			name.textboxKeyTyped(typedChar, keyCode);
			
			save.enabled = name.getText().length() > 0;
		}
	}

	public class AccountMenuData implements IScreenData
	{
		public BankAccountData bankAccount;
		public boolean canTransfer;
		GuiButtonExt withdraw;
		GuiButtonExt transfer;
		GuiButtonExt deposit;
		GuiButtonExt history;

		@Override
		public void init() {
			withdraw = new GuiButtonExt(0, horizontalCenter - 50, verticalCenter - 55, 100, 20, "Withdrawal");
			transfer = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter - 30, 100, 20, "Transfer");
			deposit = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter - 5, 100, 20, "Deposit");
			history = new GuiButtonExt(3, horizontalCenter - 50, verticalCenter + 20, 100, 20, "History");
			
			buttonList.add(withdraw);
			buttonList.add(transfer);
			buttonList.add(deposit);
			buttonList.add(history);
		}

		@Override
		public void onScreenChangedTo() {
			headerText = bankAccount.getBankAccountName();
			
			withdraw.visible=true;
			transfer.visible=true;
			transfer.enabled = canTransfer;
			deposit.visible = true;
			history.visible=true;
		}

		@Override
		public void draw(int mouseX, int mouseY)
		{
			GlStateManager.scale((3/4D), (3/4D), 0);
			
			String baNum = bankAccount.getBankAccountNumber();
			String accountNumber = String.format("Account Number: %s-%s-%s-%s", baNum.substring(0, 4), baNum.substring(4, 8), baNum.substring(8, 12), baNum.substring(12, 16));
			int textWidth = fontRenderer.getStringWidth(accountNumber);
			fontRenderer.drawString(accountNumber, (int)((texLeft + texWidth) * (1 / (3/4D))) - textWidth, (int)(((texTop + texHeight) * (1/(3/4D)))) - fontRenderer.FONT_HEIGHT, 0x5555FF);
			NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
			String balance = String.format("Balance: %s", format.format(bankAccount.getBalance()));
			textWidth = fontRenderer.getStringWidth(balance);
			fontRenderer.drawString(balance, (int)((texLeft + texWidth) * (1/(3/4D))) - textWidth, (int)(((texTop + texHeight)*(1/(3/4D)))) - (fontRenderer.FONT_HEIGHT * 2), 0x5555FF);
			
			GlStateManager.scale(1 / (3 / 4D), 1 / (3 / 4D), 0);
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 0:
					((WithdrawalData)screenDataForScreen.get(Screens.Withdraw)).bankAccount = bankAccount;
					currentScreen = Screens.Withdraw;
					break;
				case 1:
					TransferRetrieveBankAccounts retrieve = new TransferRetrieveBankAccounts();
					retrieve.currentBankAccountNumber = bankAccount.getBankAccountNumber();
					
					currentScreen = Screens.Loading;
					PacketHandler.INSTANCE.sendToServer(retrieve);
					break;
				case 2:
					((DepositData)screenDataForScreen.get(Screens.Deposit)).bankAccount = bankAccount;
					currentScreen = Screens.Deposit;
					break;
				case 3:
					((HistoryData)screenDataForScreen.get(Screens.History)).bankAccount = bankAccount;
					currentScreen = Screens.History;
					break;
			}
		}
	}

	public class WithdrawalData implements IScreenData
	{
		public BankAccountData bankAccount;
		private String error;
		private boolean suppressOnScreenChangedTo = false;
		GuiButtonExt withdraw;
		GuiButtonExt cancel;
		GuiTextField withdrawAmount;

		@Override
		public void init() {
			withdrawAmount = new GuiTextField(0, fontRenderer, horizontalCenter - 50, verticalCenter + 5, 100, 20);
			withdraw = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter + 30, 100, 20, "Withdrawal");
			cancel = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter + 55, 100, 20, "Cancel");
			
			textFields.add(withdrawAmount);
			buttonList.add(withdraw);
			buttonList.add(cancel);
		}

		@Override
		public void onScreenChangedTo() {
			if (suppressOnScreenChangedTo)
			{
				suppressOnScreenChangedTo = false;
			}
			else
			{
				error = "";
				withdrawAmount.setText("");
				withdraw.enabled = false;
			}
			
			withdrawAmount.setVisible(true);
			withdraw.visible = true;
			withdrawAmount.setFocused(true);
			cancel.visible = true;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			withdrawAmount.drawTextBox();
			
			NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
			String availableFunds = formatter.format(bankAccount.getBalance());
			
			availableFunds = String.format("Available Funds: %s", availableFunds);
			int textWidth = fontRenderer.getStringWidth(availableFunds);
			
			fontRenderer.drawString(availableFunds, horizontalCenter - (textWidth / 2), verticalCenter - 5, 0xFFFFFF);
			
			textWidth = fontRenderer.getStringWidth("Amount:");
			fontRenderer.drawString("Amount:", withdrawAmount.x - textWidth - 5, verticalCenter + 10, 0xFFFFFF);
			
			if (error != null)
			{
				textWidth = fontRenderer.getStringWidth(error);
				fontRenderer.drawString(error, horizontalCenter - (textWidth / 2), verticalCenter - 18, 0xFF0000);
			}
			
			if (atm.getWithdrawalFeeAmount() != 0)
			{
				String text = String.format("Withdrawal Fee: %s", formatter.format(atm.getWithdrawalFeeAmount() / 100D));
				textWidth = fontRenderer.getStringWidth(text);
				fontRenderer.drawString(text, horizontalCenter - (textWidth / 2), verticalCenter - 28, 0xFFFFFF);
			}
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id) {
				case 1:
					double balanceToWithdraw;
					try
					{
						balanceToWithdraw = Double.parseDouble(withdrawAmount.getText());
					}
					catch(NumberFormatException ex)
					{
						error = "Not a number!";
						return;
					}
					
					if (balanceToWithdraw <= 0)
					{
						error = "Number must be greater than 0!";
						return;
					}
					
					if (balanceToWithdraw > bankAccount.getBalance() + (atm.getWithdrawalFeeAmount() / 100D))
					{
						error = "Insufficient funds!";
						return;
					}

					currentScreen = Screens.Loading;
					
					Withdrawal withdrawal = new Withdrawal();
					withdrawal.bankAccountNumber = bankAccount.getBankAccountNumber();
					withdrawal.amount = balanceToWithdraw;
					withdrawal.fee = atm.getWithdrawalFeeAmount();
					withdrawal.pos = atm.getPos();
					PacketHandler.INSTANCE.sendToServer(withdrawal);
					break;
				case 2:
					currentScreen = Screens.AccountMenu;
					break;
			}
		}
		
		@Override
		public void keyTyped(char typedChar, int keyCode) {
			withdrawAmount.textboxKeyTyped(typedChar, keyCode);
			try
			{
				String textValue = withdrawAmount.getText();
				Double.parseDouble(textValue);
				
				if (textValue.contains("."))
				{
					textValue = textValue.substring(textValue.indexOf('.') + 1);
					if (textValue.length() > 2 || textValue.length() == 0)
					{
						withdraw.enabled = false;
						return;
					}
				}
				withdraw.enabled = true;
			}
			catch(NumberFormatException ex)
			{
				withdraw.enabled = false;
			}
		}
		
		@Override
		public void mouseClick(int mouseX, int mouseY, int mouseButton) {
			withdrawAmount.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		public void setError(String error)
		{
			cancel.enabled = true;
			
			this.error = error;
			suppressOnScreenChangedTo = true;
		}
	}

	public class SuccessData implements IScreenData
	{
		private int counter = 0;
		@Override
		public void init() { }

		@Override
		public void onScreenChangedTo() 
		{
			counter = 0;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			counter++;
			
			if (counter > 100)
			{
				Minecraft.getMinecraft().displayGuiScreen(null);
				return;
			}
			
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("economycontrol:textures/gui/thank-you.png"));
			drawModalRectWithCustomSizedTexture(horizontalCenter - 10, verticalCenter - 20, 0, 0, 20, 20, 20, 20);
			
			int fontWidth = fontRenderer.getStringWidth("Thank you for using our service!");
			fontRenderer.drawString("Thank you for using our service!", horizontalCenter - (fontWidth / 2), verticalCenter + 5, 0xFFFFFF);
		}		
	}

	public class TransferAccountSelectionData implements IScreenData
	{
		public BankAccountData currentBankAccount;
		public List<BankAccountData> allBankAccounts;
		
		GuiButtonExt account1;
		GuiButtonExt account2;
		GuiButtonExt account3;
		GuiButtonExt account4;
		GuiButtonExt prev;
		GuiButtonExt next;
		int skip;
		
		@Override
		public void init() {
			account1 = new GuiButtonExt(0, horizontalCenter - 50, verticalCenter - 55, 100, 20, "");
			account2 = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter - 25, 100, 20, "");
			account3 = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter + 5, 100, 20, "");
			account4 = new GuiButtonExt(3, horizontalCenter - 50, verticalCenter + 35, 100, 20, "");
			prev = new GuiButtonExt(5, horizontalCenter - 50, verticalCenter + 65, 20, 20, "<");
			next = new GuiButtonExt(6, horizontalCenter + 30, verticalCenter + 65, 20, 20, ">");
			
			buttonList.add(account1);
			buttonList.add(account2);
			buttonList.add(account3);
			buttonList.add(account4);
			buttonList.add(prev);
			buttonList.add(next);
		}

		@Override
		public void onScreenChangedTo() {
			account1.visible = false;
			account2.visible = false;
			account3.visible = false;
			account4.visible = false;
			prev.visible = true;
			next.visible = true;
			
			prev.enabled = skip != 0;
			next.enabled = allBankAccounts.size() > skip + 4;
			
			for(int i = 0; i < 4; i++)
			{
				if (i + skip >= allBankAccounts.size())
				{
					break;
				}
				
				BankAccountData data = allBankAccounts.get(i + skip);
				String display = String.format("%s (%s)", data.getBankAccountName(), data.getBankAccountType().getFriendlyName());
				boolean enabled = !data.getBankAccountNumber().equals(currentBankAccount.getBankAccountNumber());
				
				GuiButtonExt button = null;
				switch(i)
				{
					case 0:
						button = account1;
						break;
					case 1:
						button = account2;
						break;
					case 2:
						button = account3;
						break;
					case 3:
						button = account4;
						break;
				}
				
				if (button == null)
				{
					continue;
				}
				
				button.displayString = display;
				button.enabled = enabled;
				button.visible = true;
			}
			
			headerText = "Select To Account";
		}

		@Override
		public void draw(int mouseX, int mouseY) { }
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 0:
					BankAccountData account1 = allBankAccounts.get(skip);
					TransferAmountData data1 = (TransferAmountData)screenDataForScreen.get(Screens.TransferAmount);
					data1.accountFrom = currentBankAccount;
					data1.accountTo = account1;
					currentScreen = Screens.TransferAmount;
					break;
				case 1:
					BankAccountData account2 = allBankAccounts.get(skip + 1);
					TransferAmountData data2 = (TransferAmountData)screenDataForScreen.get(Screens.TransferAmount);
					data2.accountFrom = currentBankAccount;
					data2.accountTo = account2;
					currentScreen = Screens.TransferAmount;
					
					break;
				case 2:
					BankAccountData account3 = allBankAccounts.get(skip + 2);
					TransferAmountData data3 = (TransferAmountData)screenDataForScreen.get(Screens.TransferAmount);
					data3.accountFrom = currentBankAccount;
					data3.accountTo = account3;
					currentScreen = Screens.TransferAmount;
					
					break;
				case 3:
					BankAccountData account4 = allBankAccounts.get(skip + 3);
					TransferAmountData data4 = (TransferAmountData)screenDataForScreen.get(Screens.TransferAmount);
					data4.accountFrom = currentBankAccount;
					data4.accountTo = account4;
					currentScreen = Screens.TransferAmount;
					
					break;
				case 5:
					if (skip > 0)
					{
						skip -= 4;
						
						if (skip < 0)
						{
							skip = 0;
						}
					}
					
					prev.enabled = skip == 0;
					onScreenChangedTo();
					break;
				case 6:
					if (skip + 4 <= allBankAccounts.size())
					{
						skip += 4;
					}
					
					next.enabled = skip + 4 > allBankAccounts.size();
					onScreenChangedTo();
					break;
			}
		}
	}

	public class TransferAmountData implements IScreenData
	{
		public BankAccountData accountFrom;
		public BankAccountData accountTo;
		
		private boolean suppressDataReset = false;
		private String error;
		GuiTextField amount;
		GuiButtonExt transfer;
		GuiButtonExt cancel;
		
		@Override
		public void init() {
			amount = new GuiTextField(0, fontRenderer, horizontalCenter - 50, verticalCenter - 10, 100, 20);
			transfer = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter + 15, 100, 20, "Transfer");
			cancel = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter + 40, 100, 20, "Cancel");
			
			buttonList.add(transfer);
			buttonList.add(cancel);
			textFields.add(amount);
		}

		@Override
		public void onScreenChangedTo() {
			if (suppressDataReset)
			{
				suppressDataReset= false;
			}
			else
			{
				amount.setText("");
				error = "";
				transfer.enabled = false;
			}
			amount.setVisible(true);
			amount.setFocused(true);
			transfer.visible = true;
			cancel.visible = true;
			
			headerText = accountFrom.getBankAccountName() + " -> " + accountTo.getBankAccountName();
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			amount.drawTextBox();
			
			int textWidth = fontRenderer.getStringWidth("Amount:");
			fontRenderer.drawString("Amount:", horizontalCenter - 50 - textWidth - 5, verticalCenter - 5, 0xFFFFFF);
			
			NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
			String text = String.format("%s Funds: %s", accountTo.getBankAccountName(), formatter.format(accountTo.getBalance()));
			textWidth = fontRenderer.getStringWidth(text);
			fontRenderer.drawString(text, horizontalCenter - (textWidth / 2), verticalCenter - 25, 0xFFFFFF);
			
			text = String.format("%s Funds: %s", accountFrom.getBankAccountName(), formatter.format(accountFrom.getBalance()));
			textWidth = fontRenderer.getStringWidth(text);
			fontRenderer.drawString(text, horizontalCenter - (textWidth / 2), verticalCenter - 40, 0xFFFFFF);
			
			if (error != null)
			{
				textWidth = fontRenderer.getStringWidth(error);
				fontRenderer.drawString(error, horizontalCenter - (textWidth / 2), verticalCenter + 65, 0xFF0000);
			}
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 1:
					double enteredAmount;
					try
					{
						String amountText = amount.getText();
						enteredAmount = Double.parseDouble(amountText);
						
						int decimals = -1;
						if (amountText.contains("."))
						{
							amountText = amountText.substring(amountText.indexOf('.') + 1);
							decimals = amountText.length();
						}
						
						if (decimals > 2 || decimals == 0)
						{
							error = "Invalid number!";
							return;
						}
					}
					catch(NumberFormatException ex)
					{
						error = "Not a number!";
						return;
					}
					
					if (enteredAmount < 0)
					{
						error = "Amount must be greater than 0!";
						return;
					}
					
					if (enteredAmount > accountFrom.getBalance())
					{
						error = "Insufficient funds!";
						return;
					}
					
					TransferAmount transfer = new TransferAmount();
					transfer.bankAccountFrom = accountFrom.getBankAccountNumber();
					transfer.bankAccountTo = accountTo.getBankAccountNumber();
					transfer.amount = enteredAmount;
					transfer.pos = atm.getPos();
					
					currentScreen = Screens.Loading;
					PacketHandler.INSTANCE.sendToServer(transfer);
					break;
				case 2:
					currentScreen = Screens.AccountMenu;
					break;
			}
		}
		
		@Override
		public void mouseClick(int mouseX, int mouseY, int mouseButton) {
			amount.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		public void keyTyped(char typedChar, int keyCode) {
			if (amount.textboxKeyTyped(typedChar, keyCode))
			{
				try
				{
					String amountText = amount.getText();
					Double.parseDouble(amountText);
					
					int decimals = -1;
					
					if (amountText.contains("."))
					{
						amountText = amountText.substring(amountText.indexOf('.') + 1);
						decimals = amountText.length();
					}
					
					transfer.enabled = decimals != 0 && decimals <= 2;
				}
				catch(NumberFormatException ex)
				{
					transfer.enabled = false;
				}
			}
		}
	
		public void setError(String error)
		{
			suppressDataReset = true;
			this.error = error;
		}
	}

	public class DepositData implements IScreenData
	{
		public BankAccountData bankAccount;
		
		boolean suppressDataReset;
		String error;
		double amountAvailable;
		GuiTextField amount;
		GuiButtonExt deposit;
		GuiButtonExt cancel;
		@Override
		public void init() {
			amount = new GuiTextField(0, fontRenderer, horizontalCenter - 50, verticalCenter - 10, 100, 20);
			deposit = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter + 15, 100, 20, "Deposit");
			cancel = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter + 40, 100, 20, "Cancel");
			
			textFields.add(amount);
			buttonList.add(deposit);
			buttonList.add(cancel);
		}

		@Override
		public void onScreenChangedTo() {
			if (suppressDataReset)
			{
				suppressDataReset = false;
			}
			else
			{
				amount.setText("");
				error = "";
				deposit.enabled=false;
			}
			
			calculateAmountAvailable();
			amount.setVisible(true);
			deposit.visible=true;
			cancel.visible=true;
		}
		
		private void calculateAmountAvailable()
		{
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);
				if (stackInSlot != null && stackInSlot.getItem() == ModItems.money && stackInSlot.getMetadata() < MoneyTypes.values().length)
				{
					MoneyTypes moneyType = MoneyTypes.values()[stackInSlot.getMetadata()];
					amountAvailable += (moneyType.getValue() * stackInSlot.getCount());
				}
			}
			
			amountAvailable /= 100;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			amount.drawTextBox();
			
			NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
			String availableText = String.format("Amount Available: %s", format.format(amountAvailable));
			int textWidth = fontRenderer.getStringWidth(availableText);
			fontRenderer.drawString(availableText, horizontalCenter - (textWidth / 2), verticalCenter - 25, 0xFFFFFF);
			
			textWidth = fontRenderer.getStringWidth("Amount:");
			fontRenderer.drawString("Amount:", horizontalCenter - 50 - textWidth - 5, verticalCenter - 5, 0xFFFFFF);
			
			if (error != null)
			{
				textWidth = fontRenderer.getStringWidth(error);
				fontRenderer.drawString(error, horizontalCenter - (textWidth / 2), verticalCenter - 38, 0xFF0000);
			}
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 1:
					double enteredAmount;
					try
					{
						String amountText = amount.getText();
						enteredAmount = Double.parseDouble(amountText);
						
						int decimals = -1;
						if (amountText.contains("."))
						{
							amountText = amountText.substring(amountText.indexOf('.') + 1);
							decimals = amountText.length();
						}

						if (decimals == 0 || decimals > 2)
						{
							error = "Invalid number!";
							return;
						}
						
						DepositAmount deposit = new DepositAmount();
						deposit.account = bankAccount.getBankAccountNumber();
						deposit.amount = enteredAmount;
						deposit.pos = atm.getPos();
						
						currentScreen = Screens.Loading;
						PacketHandler.INSTANCE.sendToServer(deposit);
					}
					catch(NumberFormatException ex)
					{
						error = "Not a number!";
						return;
					}
					
					if (enteredAmount < 0)
					{
						error = "Amount must be greater than 0!";
						break;
					}
					
					if (enteredAmount > amountAvailable)
					{
						error = "Not enough money!";
						return;
					}
					
					
					break;
				case 2:
					currentScreen = Screens.AccountMenu;
					break;
			}
		}
		
		@Override
		public void keyTyped(char typedChar, int keyCode) {
			if (amount.textboxKeyTyped(typedChar, keyCode))
			{
				try
				{
					String amountText = amount.getText();
					Double.parseDouble(amountText);
					
					int decimals = -1;
					if (amountText.contains("."))
					{
						amountText = amountText.substring(amountText.indexOf('.') + 1);
						decimals = amountText.length();
					}
					
					if (decimals == 0 || decimals > 2)
					{
						deposit.enabled = false;
						return;
					}
				}
				catch(NumberFormatException ex)
				{
					deposit.enabled = false;
				}
				
				deposit.enabled = !amount.getText().equals("");
			}
		}
		
		@Override
		public void mouseClick(int mouseX, int mouseY, int mouseButton) {
			amount.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		public void setError(String error)
		{
			this.error = error;
			suppressDataReset = true;
		}
	}

	public class InitializeFeeAccountData implements IScreenData
	{
		public List<BankAccountData> bankAccounts;
		int skip;
		
		GuiButtonExt account1;
		GuiButtonExt account2;
		GuiButtonExt account3;
		GuiButtonExt account4;
		GuiButtonExt prev;
		GuiButtonExt next;
		
		@Override
		public void init() {
			account1 = new GuiButtonExt(0, horizontalCenter - 50, verticalCenter - 50, 100, 20, "");
			account2 = new GuiButtonExt(1, horizontalCenter - 50, verticalCenter - 25, 100, 20, "");
			account3 = new GuiButtonExt(2, horizontalCenter - 50, verticalCenter, 100, 20, "");
			account4 = new GuiButtonExt(3, horizontalCenter - 50, verticalCenter + 25, 100, 20, "");
			prev = new GuiButtonExt(4, horizontalCenter - 50, verticalCenter + 50, 20, 20, "<");
			next = new GuiButtonExt(5, horizontalCenter + 30, verticalCenter + 50, 20, 20, ">");
			
			buttonList.add(account1);
			buttonList.add(account2);
			buttonList.add(account3);
			buttonList.add(account4);
			buttonList.add(prev);
			buttonList.add(next);
		}

		@Override
		public void onScreenChangedTo() {
			headerText = "Select Fee Account";
			
			GuiButtonExt[] accountBtns = new GuiButtonExt[] { account1, account2, account3, account4 };
			
			for(int i = 0; i < 4; i++)
			{
				if (i + skip >= bankAccounts.size())
				{
					break;
				}
				
				BankAccountData data = bankAccounts.get(i);
				GuiButtonExt accountButton = accountBtns[i];
				accountButton.visible=true;
				accountButton.displayString = String.format("%s (%s)", data.getBankAccountName(), data.getBankAccountType().getFriendlyName());
			}
			
			prev.visible=true;
			next.visible=true;
			prev.enabled = skip != 0;
			next.enabled = skip + 4 < bankAccounts.size();
		}

		@Override
		public void draw(int mouseX, int mouseY) { }
		
		@Override
		public void buttonClick(GuiButton button) {
			BankAccountData data = null;
			switch(button.id)
			{
				case 0:
				case 1:
				case 2:
				case 3:
					data = bankAccounts.get(button.id + skip);
					break;
				case 4:
					if (skip != 0)
					{
						skip -= 4;
						
						if (skip < 0)
						{
							skip = 0;
						}
					}
					
					onScreenChangedTo();
					return;
				case 5:
					if (skip + 4 < bankAccounts.size())
					{
						skip += 4;
					}
					
					onScreenChangedTo();
					return;
			}
			
			atm.setFeeAccountNumber(data.getBankAccountNumber());
			atm.syncWithServer();
			
			Initialize init = new Initialize();
			init.pos = atm.getPos();
			currentScreen = Screens.Loading;
			PacketHandler.INSTANCE.sendToServer(init);
		}
	}

	public class HistoryData implements IScreenData
	{
		public BankAccountData bankAccount;
		List<History> histories = new ArrayList<>();
		boolean loading = true;
		
		GuiButtonExt back;
		GuiButtonExt prev;
		GuiButtonExt next;
		int skip = 0;
		int[] rowHeights;
		int[] columnStarts;
		
		@Override
		public void init() {
			back = new GuiButtonExt(0, texLeft + 25, texTop + texHeight - 20, texWidth - 50, 20, "Back");
			prev = new GuiButtonExt(1, texLeft + 5, texTop + texHeight - 20, 20, 20, "<");
			next = new GuiButtonExt(2, texLeft + texWidth - 25, texTop + texHeight - 20, 20, 20, ">");
			
			buttonList.add(back);
			buttonList.add(prev);
			buttonList.add(next);
			
			rowHeights = new int[14];
			rowHeights[0] = texTop + 30;
			for (int i = 1; i < 14; i++)
			{
				rowHeights[i] = rowHeights[i - 1] + 10;
			}
			
			columnStarts = new int[]
			{
				texLeft + 1,
				texLeft + 50,
				texLeft + 110,
				texLeft + texWidth - 1 - fontRenderer.getStringWidth("\u00a7lAmount") // Right Align
			};
		}

		@Override
		public void onScreenChangedTo() {
			headerText = "";
			next.visible = false;
			prev.visible = false;
			back.visible = true;
			
			RetrieveHistories request = new RetrieveHistories();
			request.bankAccountNumber = bankAccount.getBankAccountNumber();
			request.amount = 13;
			request.skip = skip;
			PacketHandler.INSTANCE.sendToServer(request);
			
			loading = true;
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			int textWidth = 0;
			fontRenderer.drawString("\u00a7lDate", columnStarts[0], rowHeights[0], 0xFFFFFF);
			fontRenderer.drawString("\u00a7lPlayer", columnStarts[1], rowHeights[0], 0xFFFFFF);
			fontRenderer.drawString("\u00a7lDescription", columnStarts[2], rowHeights[0], 0xFFFFFF);
			fontRenderer.drawString("\u00a7lAmount", columnStarts[3], rowHeights[0], 0xFFFFFF);
			
			GlStateManager.disableTexture2D();
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder builder = tess.getBuffer();
			builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			
			builder.pos(texLeft, rowHeights[0] + fontRenderer.FONT_HEIGHT, 5).color(255, 255, 255, 255).endVertex();
			builder.pos(texLeft + texWidth, rowHeights[0] + fontRenderer.FONT_HEIGHT, 5).color(255, 255, 255, 255).endVertex();
			tess.draw();
			GlStateManager.enableTexture2D();
			
			if (loading)
			{
				textWidth = fontRenderer.getStringWidth("Loading...");
				fontRenderer.drawString("Loading...", horizontalCenter - (rowHeights[0] / 2), verticalCenter - (fontRenderer.FONT_HEIGHT / 2), 0xFFFFFF);
			}
			else
			{
				drawHistories();
				
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					drawHistoryToolTip(mouseX, mouseY);
				}
				
				textWidth = fontRenderer.getStringWidth("Hold L-Shift to see more detail");
				fontRenderer.drawString("Hold L-Shift to see more detail", horizontalCenter - (textWidth / 2), texTop + texHeight - 29, 0xFFFF00);
			}
		}
		
		private void drawHistories()
		{
			for(int i = 0; i < histories.size(); i++)
			{
				int rowHeight = rowHeights[i + 1];
				History history = histories.get(i);
				
				String dateString = String.format("%02d-%02d", history.getTransactionDate().get(Calendar.MONTH) + 1, history.getTransactionDate().get(Calendar.DAY_OF_MONTH));
				fontRenderer.drawString(getTruncatedString(dateString, columnStarts[1] - columnStarts[0] - 1), columnStarts[0], rowHeight, 0xFFFFFF);
				fontRenderer.drawString(getTruncatedString(history.getPerformer(), columnStarts[2] - columnStarts[1] - 1), columnStarts[1], rowHeight, 0xFFFFFF);
				fontRenderer.drawString(getTruncatedString(history.getDescription(), columnStarts[3] - columnStarts[2] - 1), columnStarts[2], rowHeight, 0xFFFFFF);
				
				NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
				int textWidth = fontRenderer.getStringWidth(getTruncatedString(format.format(history.getAmount() / 100D), (texLeft + texWidth) - columnStarts[3]));
				
				fontRenderer.drawString(getTruncatedString(format.format(history.getAmount() / 100D), (texLeft + texWidth) - columnStarts[3]), (texLeft + texWidth) - textWidth - 1, rowHeight, 0xFFFFFF);
			}
		}
		
		private String getTruncatedString(String string, int maxWidth)
		{
			if (fontRenderer.getStringWidth(string) > maxWidth)
			{
				int ellipsesWidth = fontRenderer.getStringWidth("...");
				return fontRenderer.trimStringToWidth(string, maxWidth - ellipsesWidth) + "...";
			}
			
			return string;
		}
		
		private void drawHistoryToolTip(int mouseX, int mouseY)
		{
			if (mouseX >= texLeft && mouseX <= texLeft + texWidth)
			{
				if (Arrays.stream(rowHeights).anyMatch(height -> mouseY >= height && mouseY <= height + fontRenderer.FONT_HEIGHT))
				{
					int index = 0;
					for(int i = 1; i < rowHeights.length; i++)
					{
						int rowHeight = rowHeights[i];
						if (mouseY >= rowHeight && mouseY <= rowHeight + fontRenderer.FONT_HEIGHT)
						{
							index = i;
							break;
						}
					}
					
					index -= 1;
					
					if (index != -1 && index < histories.size())
					{
						History hoveringHistory = histories.get(index);
						
						int col = 0;
						
						for (int i = 0; i < columnStarts.length; i++)
						{
							if (mouseX >= columnStarts[i])
							{
								col = i;
							}
						}
						
						switch(col)
						{
							case 0:
								String date = String.format("%02d-%02d-%d", hoveringHistory.getTransactionDate().get(Calendar.MONTH) + 1, hoveringHistory.getTransactionDate().get(Calendar.DAY_OF_MONTH), hoveringHistory.getTransactionDate().get(Calendar.YEAR));
								drawHoveringText(date, mouseX, mouseY);
								break;
							case 1:
								drawHoveringText(hoveringHistory.getPerformer(), mouseX, mouseY);
								break;
							case 2:
								drawHoveringText(hoveringHistory.getDescription(), mouseX, mouseY);
								break;
							case 3:
								NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
								drawHoveringText(format.format((hoveringHistory.getAmount() / 100D)), mouseX, mouseY);
								break;
						}
						
						GlStateManager.disableLighting();
					}
				}
			}
		}
		
		@Override
		public void buttonClick(GuiButton button) {
			switch(button.id)
			{
				case 0:
					currentScreen = Screens.AccountMenu;
					break;
				case 1:
					skip -= 13;
					
					if (skip < 0)
					{
						skip = 0;
					}
					
					onScreenChangedTo();
					break;
				case 2:
					skip += 13;
					
					onScreenChangedTo();
					break;
				
			}
		}		
		
		public void setHistories(List<History> histories, boolean hasNext)
		{
			this.histories = histories;
			prev.enabled = skip != 0;
			next.enabled = hasNext;
			prev.visible=true;
			next.visible=true;
			loading = false;
		}
	}
}
