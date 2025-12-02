package iteration1.api;

import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

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
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

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
