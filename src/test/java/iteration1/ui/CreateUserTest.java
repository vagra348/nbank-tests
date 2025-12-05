package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    public void adminCanCreateUserTest(){
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authWithToken(admin);
        new AdminPanel().open().getAdminPanelText().shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .findBy(Condition.partialText(newUser.getUsername())).shouldBe(Condition.visible);

        ProfileModel createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModel(newUser, createdUser).match();
    }

    @Tag("NEGATIVE")
    @Test
    public void adminCanNotCreateUserWithInvalidDataTest(){
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authWithToken(admin);
        new AdminPanel().open().getAdminPanelText().shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CH.getMessage())
                .getAllUsers().findBy(Condition.partialText(newUser.getUsername())).shouldNotBe(Condition.exist);

        long usersWithSameUsrnmAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsrnmAsNewUser).isZero();
    }
}
