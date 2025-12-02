package iteration1.ui;

import api.models.AccountModel;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);

        new UserDashboard().open().createNewAccount();

        List<AccountModel> createdAccs = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(createdAccs).hasSize(1);

        new UserDashboard().checkAlertAndAccept(BankAlert.ACCOUNT_CREATED.getMessage() + createdAccs.getFirst().getAccountNumber());
        assertThat(createdAccs.getFirst().getBalance()).isZero();

    }
}
