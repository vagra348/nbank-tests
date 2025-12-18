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
    @UserSession
    public void userCanChangeNameWithValidDataTest() {
        String newName = RandomData.qenerateName();
        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName(newName)
                .checkAlertAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        UserDashboard dashboard = new UserDashboard();
        assertEquals(dashboard.open()
                .getWelcomeTitle().getUserName(), newName);
        dashboard.getWelcomeText().shouldBe(Condition.visible);

        ProfileModel changedUser = UserSteps.getProfile(SessionStorage.getUser());
        assertThat(changedUser.getName()).isEqualTo(newName);
    }

    @Tag("NEGATIVE")
    @Test
    @UserSession
    public void userCanNotChangeNameWithInvalidDataTest() { //иногда падает, т.к. открываем один контейнер,
                                                            //и из кэша может подтягиваться значение из предыдущего теста.
                                                            //даже вручную в браузере иногда нужно насильно чистить кэш,
                                                            //просто перезагрузка страницы не помогает
        new UserDashboard().open().getUserNameProfile().click();
        new EditProfilePage().changeName(RandomData.qenerateWord())
                .checkAlertAndAccept(BankAlert.INVALID_NAME.getMessage());

        UserDashboard dashboard = new UserDashboard();
        assertEquals(dashboard.open()
                .getWelcomeTitle().getUserName(), UserDashboard.NO_NAME_TEXT);
        dashboard.getWelcomeText().shouldBe(Condition.visible);

        ProfileModel changedUser = UserSteps.getProfile(SessionStorage.getUser());
        assertThat(changedUser.getName()).isEqualTo(null);
    }
}
