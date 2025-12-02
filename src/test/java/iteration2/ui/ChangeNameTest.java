package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    public void userCanChangeNameWithValidDataTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);

        String newName = RandomData.qenerateName();
        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName(newName)
                .checkAlertAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        new UserDashboard().open().getWelcomeText().shouldBe(Condition.visible)
                .shouldHave(Condition.partialText(newName)); //падает иногда, текст на стр обновляется долго, надо добавить ожидание

        ProfileModel changedUser = UserSteps.getProfile(user);
        assertThat(changedUser.getName()).isEqualTo(newName);
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotChangeNameWithInvalidDataTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);
        authWithToken(user);
        new UserDashboard().open();

        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName("oneWord")
                .checkAlertAndAccept(BankAlert.INVALID_NAME.getMessage());

        new UserDashboard().open().getWelcomeText().shouldBe(Condition.visible)
                .shouldHave(Condition.partialText("noname"));

        ProfileModel changedUser = UserSteps.getProfile(user);
        assertThat(changedUser.getName()).isEqualTo(null);
    }
}
