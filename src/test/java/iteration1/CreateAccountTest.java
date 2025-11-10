package iteration1;

import base.BaseTest;
import io.restassured.response.ValidatableResponse;
import models.AccountModel;
import models.CreateUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
