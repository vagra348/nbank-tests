package iteration1;

import base.BaseTest;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.LoginUserResponse;
import models.ProfileModel;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    @Tag("POSITIVE")
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
    }

    @Test
    @Tag("POSITIVE")
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(createUserRequest.getUsername())
                .password(createUserRequest.getPassword())
                .build();

        LoginUserResponse loginUserResponse = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .header("Authorization", Matchers.notNullValue())
                .extract().as(LoginUserResponse.class);

        softly.assertThat(loginUserRequest.getUsername()).isEqualTo(loginUserResponse.getUsername());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(loginUserResponse.getRole());
    }

    @Test
    @Tag("POSITIVE")
    public void authorizedAdminCanSeeUsersListTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_GET_USERS_LIST,
                ResponseSpecs.requestReturnsOK())
                .get()
                .assertThat()
                .body(Matchers.containsString(createUserRequest.getUsername()));
    }

    @Test
    @Tag("NEGATIVE")
    public void nonExistLoginTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("userThatDoesNotExist")
                .password("TestPass12345$")
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestUnauthorized())
                .post(loginUserRequest);
    }

    @Test
    @Tag("NEGATIVE")
    public void incorrectPasswordTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("admin")
                .password("adminIncorrectPass")
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestUnauthorized())
                .post(loginUserRequest);
    }

}
