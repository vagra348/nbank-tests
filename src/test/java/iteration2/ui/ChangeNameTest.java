package iteration2.ui;

import api.generators.RandomData;
import api.models.ProfileModel;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeNameTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    @Tag("ui")
    @UserSession
    public void userCanChangeNameWithValidDataTest() throws InterruptedException {
        String newName = RandomData.qenerateName();
        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName(newName, BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .checkAlertAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        UserDashboard dashboard = new UserDashboard().open();
        String actualName = dashboard.getUserNameInWelcomeText();
        assertEquals(newName, actualName);

        ProfileModel changedUser = UserSteps.getProfile(SessionStorage.getUser());
        assertThat(changedUser.getName()).isEqualTo(newName);
    }

    @Tag("NEGATIVE")
    @Test
    @Tag("ui")
    @UserSession
    public void userCanNotChangeNameWithInvalidDataTest() {
        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName(RandomData.qenerateWord(), BankAlert.INVALID_NAME.getMessage())
                .checkAlertAndAccept(BankAlert.INVALID_NAME.getMessage());

        UserDashboard dashboard = new UserDashboard();
        assertEquals(dashboard.open()
                .getWelcomeTitle().getUserName(), UserDashboard.NO_NAME_TEXT);
        dashboard.getWelcomeText().shouldBe(Condition.visible);

        ProfileModel changedUser = UserSteps.getProfile(SessionStorage.getUser());
        assertThat(changedUser.getName()).isEqualTo(null);
    }
}
