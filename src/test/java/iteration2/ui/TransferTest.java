package iteration2.ui;

import api.generators.RandomData;
import api.models.AccountModel;
import api.models.MakeDepositRequest;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.elements.Transaction;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest extends BaseUiTest {
    
    @Tag("POSITIVE")
    @Test
    @Browsers({"chrome"})
    @UserSession
    public void userCanTransferBetweenOwnAccountsTest() {
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(SessionStorage.getUser());
        AccountModel receiverAccount = UserSteps.createAccount(SessionStorage.getUser());
        Double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(SessionStorage.getUser(), makeDepositRequest);
        Double transferAmount = RandomData.generateSum(1.0, 1999.0);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(SessionStorage.getUser(), senderAccount, receiverAccount, transferAmount)
                .checkAlertAndAccept(BankAlert.SUCCESSFULLY_TRANSFERED.getMessage());

        TransferPage transferPage = new TransferPage();
        transferPage.open();
        transferPage.getTransferAgainBtn().click();
        Transaction transactionElement = transferPage.getTransactionsElementsList().stream()
                .filter(transaction -> transaction.getTransactionType().equals(TransferPage.TRANSFER_OUT_TEXT))
                .findFirst().get();
        assertEquals(transactionElement.getAmount(), transferAmount, 0.1);
        transferPage.getAllTransactionsList().stream().findFirst().get().shouldBe(Condition.visible);

        List<AccountModel> accounts = new UserSteps(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword())
                .getAllAccounts();

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() - transferAmount);
        assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferAmount);
    }

    @Tag("NEGATIVE")
    @Test
    @UserSession
    public void userCanNotTransferWithoutEnteredSumTest() {
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(SessionStorage.getUser());
        AccountModel receiverAccount = UserSteps.createAccount(SessionStorage.getUser());
        Double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(SessionStorage.getUser(), makeDepositRequest);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(SessionStorage.getUser(), senderAccount, receiverAccount, null)
                .checkAlertAndAccept(BankAlert.FILL_ALL_FIELDS.getMessage());

        List<AccountModel> accounts = new UserSteps(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword())
                .getAllAccounts();
        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        assertThat(changedReceiverAcc.getBalance()).isZero();
    }


    @Tag("NEGATIVE")
    @Test
    @UserSession
    public void userCanNotTransferMoreThanBalanceTest() {
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(SessionStorage.getUser());
        AccountModel receiverAccount = UserSteps.createAccount(SessionStorage.getUser());
        Double depositAmount = RandomData.generateSum(1000.0, 2000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(SessionStorage.getUser(), makeDepositRequest);
        Double transferAmount = RandomData.generateSum(2001.0, 5000.0);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(SessionStorage.getUser(), senderAccount, receiverAccount, transferAmount)
                .checkAlertAndAccept(BankAlert.INVALID_TRANSFER.getMessage());

        List<AccountModel> accounts = new UserSteps(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword())
                .getAllAccounts();
        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        assertThat(changedReceiverAcc.getBalance()).isZero();
    }

}
