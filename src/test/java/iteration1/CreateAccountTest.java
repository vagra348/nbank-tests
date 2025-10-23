package iteration1;

import base.BaseTest;
import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import models.CreateUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetUserAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;


public class CreateAccountTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanSeeHisAccounts() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post();

        new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .body(Matchers.containsString("id"))
                .body(Matchers.containsString("accountNumber"))
                .body(Matchers.containsString("balance"))
                .body(Matchers.containsString("transactions"));
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanCreateAccount() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        Integer accIdValue = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .body(Matchers.containsString("id"))
                .body(Matchers.containsString("accountNumber"))
                .extract().body().path("id");

        ValidatableResponse response = new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get();

        softly.assertThat(response.body("id", Matchers.hasItem(accIdValue)));
    }

}
