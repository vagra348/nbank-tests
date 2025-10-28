package iteration1;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.AdminGetUsersListRequester;
import requests.LoginUserRequester;
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

        new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
    }

    @Test
    @Tag("POSITIVE")
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(createUserRequest.getUsername())
                .password(createUserRequest.getPassword())
                .build();

        LoginUserResponse loginUserResponse = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
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
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        new AdminGetUsersListRequester(
                RequestSpecs.adminSpec(),
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

        new LoginUserRequester(
                RequestSpecs.unauthSpec(),
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

        new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestUnauthorized())
                .post(loginUserRequest);
    }

}
