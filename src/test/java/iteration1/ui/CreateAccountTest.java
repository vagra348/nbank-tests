package iteration1.ui;

import api.models.AccountModel;
import base.BaseUiTest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
//    @Browsers({"firefox", "opera", "safari"})
    @UserSession
    public void userCanCreateAccountTest() {

        new UserDashboard().open().createNewAccount();

        List<AccountModel> createdAccs = SessionStorage.getSteps().getAllAccounts();
        assertThat(createdAccs).hasSize(1);

        new UserDashboard().checkAlertAndAccept(BankAlert.ACCOUNT_CREATED.getMessage() + createdAccs.getFirst().getAccountNumber());
        assertThat(createdAccs.getFirst().getBalance()).isZero();

    }
}
