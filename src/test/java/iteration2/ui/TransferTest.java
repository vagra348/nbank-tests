package iteration2.ui;

import api.generators.RandomData;
import api.models.AccountModel;
import api.models.CreateUserRequest;
import api.models.MakeDepositRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUiTest {
    
    @Tag("POSITIVE")
    @Test
    public void userCanTransferBetweenOwnAccountsTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(user);
        AccountModel receiverAccount = UserSteps.createAccount(user);
        Double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(user, makeDepositRequest);
        Double transferAmount = RandomData.generateSum(1.0, 1999.0);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(user, senderAccount, receiverAccount, transferAmount)
                .checkAlertAndAccept(BankAlert.SUCCESSFULLY_TRANSFERED.getMessage());

        Selenide.open("/transfer");
        new TransferPage().getTransferAgainBtn().click();
        new TransferPage().getAllTransactionsList()
                .findBy(Condition.partialText(String.valueOf(transferAmount.intValue())))
                .shouldBe(Condition.visible);

        List<AccountModel> accounts = new UserSteps(user.getUsername(), user.getPassword())
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
    public void userCanNotTransferWithoutEnteredSumTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(user);
        AccountModel receiverAccount = UserSteps.createAccount(user);
        Double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(user, makeDepositRequest);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(user, senderAccount, receiverAccount, null)
                .checkAlertAndAccept(BankAlert.FILL_ALL_FIELDS.getMessage());

        List<AccountModel> accounts = new UserSteps(user.getUsername(), user.getPassword())
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
    public void userCanNotTransferMoreThanBalanceTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);
        new UserDashboard().open();

        AccountModel senderAccount = UserSteps.createAccount(user);
        AccountModel receiverAccount = UserSteps.createAccount(user);
        Double depositAmount = RandomData.generateSum(1000.0, 2000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(user, makeDepositRequest);
        Double transferAmount = RandomData.generateSum(2001.0, 5000.0);

        new UserDashboard().open().getTransferMoneyBtn().click();
        new TransferPage().open().makeTransfer(user, senderAccount, receiverAccount, transferAmount)
                .checkAlertAndAccept(BankAlert.INVALID_TRANSFER.getMessage());

        List<AccountModel> accounts = new UserSteps(user.getUsername(), user.getPassword())
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
