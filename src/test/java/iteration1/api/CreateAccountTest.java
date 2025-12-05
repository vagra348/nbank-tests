package iteration1.api;

import api.models.AccountModel;
import api.models.CreateUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;


public class CreateAccountTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanSeeHisAccounts() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        new CrudRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        List<AccountModel> accounts = List.of(new CrudRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.GET_USER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));
        //если ответ десериализуется в json, то это ведь и будет являться проверкой, да?
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanCreateAccount() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel account = new ValidatedCrudRequester<AccountModel>(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);


        ValidatableResponse response = new CrudRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.GET_USER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get();


        softly.assertThat(response.body("id", Matchers.hasItem(account.getId())));
    }

}
