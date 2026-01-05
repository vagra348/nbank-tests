package iteration1.ui;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginUserTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open()
                .login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible);
    }

    @Tag("POSITIVE")
    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);

        new LoginPage().open()
                .login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text(UserDashboard.WELCOME_NONAME_TEXT));
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotLoginWithIncorrectDataTest() {
        new LoginPage().open()
                .login("nonExistUser","somePass1$");

        Alert alert = switchTo().alert();
        assertEquals(alert.getText(), "Invalid credentialsAxiosError: Request failed with status code 401");
    }
}
