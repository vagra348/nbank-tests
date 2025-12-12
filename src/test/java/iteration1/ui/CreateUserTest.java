package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import base.BaseUiTest;
import com.codeborne.selenide.Condition;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUiTest {

    @Tag("POSITIVE")
    @Test
    @AdminSession
    public void adminCanCreateUserTest(){
        new AdminPanel().open().getAdminPanelText().shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        assertTrue(new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .getAllUsers()
                .stream().anyMatch(userBadge -> userBadge.getUsername().equals(newUser.getUsername())));

        ProfileModel createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModel(newUser, createdUser).match();
    }

    @Tag("NEGATIVE")
    @Test
    @AdminSession
    public void adminCanNotCreateUserWithInvalidDataTest(){
        new AdminPanel().open().getAdminPanelText().shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");
        assertTrue(new AdminPanel().open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CH.getMessage())
                .getAllUsers().stream().noneMatch(userBadge -> userBadge.getUsername().equals(newUser.getUsername())));

        long usersWithSameUsrnmAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsrnmAsNewUser).isZero();
    }
}
