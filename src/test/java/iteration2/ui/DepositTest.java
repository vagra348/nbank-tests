package iteration2.ui;

import api.generators.RandomData;
import api.models.AccountModel;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    @Tag("ui")
    @UserSession
    public void userCanMakeDepositWithValidAmountTest() {
        AccountModel newAcc = UserSteps.createAccount(SessionStorage.getUser());
        Double sum = RandomData.generateSum(1.0, 5000.0);

        new UserDashboard().open().getDepositMoneyBtn().click();
        new DepositPage().open().makeDeposit(newAcc, sum)
                .checkAlertAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.getMessage());

        List<AccountModel> createdAccs = new UserSteps(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword())
                .getAllAccounts();
        assertThat(createdAccs).hasSize(1);
        assertThat(createdAccs.getFirst().getBalance()).isEqualTo(sum);
    }

    @Tag("NEGATIVE")
    @Test
    @Tag("ui")
    @UserSession
    public void userCanNotMakeDepositWithInvalidAmountTest() {
        new UserDashboard().open();

        AccountModel newAcc = UserSteps.createAccount(SessionStorage.getUser());

        Double sum = RandomData.generateSum(5000.0, 10000.0);
        new UserDashboard().open().getDepositMoneyBtn().click();
        new DepositPage().open().makeDeposit(newAcc, sum)
                .checkAlertAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUAL_TO_5000.getMessage());

        List<AccountModel> createdAccs = new UserSteps(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword())
                .getAllAccounts();
        assertThat(createdAccs).hasSize(1);
        assertThat(createdAccs.getFirst().getBalance()).isZero();
    }

    @Tag("NEGATIVE")
    @Test
    @Tag("ui")
    @UserSession
    public void userCanNotMakeDepositWithoutEnteredSumTest() {
        new UserDashboard().open();

        AccountModel newAcc = UserSteps.createAccount(SessionStorage.getUser());

        new UserDashboard().open().getDepositMoneyBtn().click();
        new DepositPage().open().makeDeposit(newAcc, null)
                .checkAlertAndAccept(BankAlert.ENTER_VALID_AMOUNT.getMessage());
    }
}
