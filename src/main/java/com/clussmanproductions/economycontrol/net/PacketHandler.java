package com.clussmanproductions.economycontrol.net;

import com.clussmanproductions.economycontrol.net.atm.AddBankAccount;
import com.clussmanproductions.economycontrol.net.atm.DepositAmount;
import com.clussmanproductions.economycontrol.net.atm.DepositAmountErrorResponse;
import com.clussmanproductions.economycontrol.net.atm.ErrorResponse;
import com.clussmanproductions.economycontrol.net.atm.Initialize;
import com.clussmanproductions.economycontrol.net.atm.InitializeResponse;
import com.clussmanproductions.economycontrol.net.atm.InitializeRetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.InitializeRetrieveBankAccountsResponse;
import com.clussmanproductions.economycontrol.net.atm.RetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.RetrieveBankAccountsResponse;
import com.clussmanproductions.economycontrol.net.atm.RetrieveHistories;
import com.clussmanproductions.economycontrol.net.atm.RetrieveHistoriesResponse;
import com.clussmanproductions.economycontrol.net.atm.SuccessResponse;
import com.clussmanproductions.economycontrol.net.atm.TransferAmount;
import com.clussmanproductions.economycontrol.net.atm.TransferAmountErrorResponse;
import com.clussmanproductions.economycontrol.net.atm.TransferRetrieveBankAccounts;
import com.clussmanproductions.economycontrol.net.atm.TransferRetrieveBankAccountsResponse;
import com.clussmanproductions.economycontrol.net.atm.Withdrawal;
import com.clussmanproductions.economycontrol.net.atm.WithdrawalError;
import com.clussmanproductions.economycontrol.net.financemanagement.GetFinanceContext;
import com.clussmanproductions.economycontrol.net.financemanagement.GetFinanceContextResponse;
import com.clussmanproductions.economycontrol.net.taggingstation.ValueUpdated;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	private static int packetId = 0;
	
	public static SimpleNetworkWrapper INSTANCE = null;
	
	public static int nextID()
	{
		return packetId++;
	}
	
	public static void registerMessages(String channelName)
	{
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
		registerMessages();
	}
	
	private static void registerMessages()
	{
		INSTANCE.registerMessage(PacketTileEntitySync.Handler.class, PacketTileEntitySync.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(RetrieveBankAccounts.Handler.class, RetrieveBankAccounts.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(RetrieveBankAccountsResponse.Handler.class, RetrieveBankAccountsResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(ErrorResponse.Handler.class, ErrorResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(AddBankAccount.Handler.class, AddBankAccount.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(SuccessResponse.Handler.class, SuccessResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(Withdrawal.Handler.class, Withdrawal.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(WithdrawalError.Handler.class, WithdrawalError.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(TransferRetrieveBankAccounts.Handler.class, TransferRetrieveBankAccounts.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(TransferRetrieveBankAccountsResponse.Handler.class, TransferRetrieveBankAccountsResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(TransferAmount.Handler.class, TransferAmount.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(TransferAmountErrorResponse.Handler.class, TransferAmountErrorResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(DepositAmount.Handler.class, DepositAmount.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(DepositAmountErrorResponse.Handler.class, DepositAmountErrorResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(Initialize.Handler.class, Initialize.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(InitializeResponse.Handler.class, InitializeResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(InitializeRetrieveBankAccounts.Handler.class, InitializeRetrieveBankAccounts.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(InitializeRetrieveBankAccountsResponse.Handler.class, InitializeRetrieveBankAccountsResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(RetrieveHistories.Handler.class, RetrieveHistories.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(RetrieveHistoriesResponse.Handler.class, RetrieveHistoriesResponse.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(ValueUpdated.Handler.class, ValueUpdated.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(GetFinanceContext.Handler.class, GetFinanceContext.class, nextID(), Side.SERVER);
		INSTANCE.registerMessage(GetFinanceContextResponse.Handler.class, GetFinanceContextResponse.class, nextID(), Side.CLIENT);
	}
}
