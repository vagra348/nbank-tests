package test_with_mocks.api;

import api.models.AccountModel;
import api.models.comparison.ModelAssertions;
import base.WireMockTest;
import common.annotations.MockWith;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

@MockWith({
        "accounts/create-account-success.json",
        "accounts/get-user-accounts.json"
})
public class CreateAccountMockTest extends WireMockTest {

    @Test @Disabled
    @Tag("mock")
    @Tag("wiremock")
    public void createAccountWithMock() {
        AccountModel newAcc = RestAssured.given()
                .header("Authorization", "Bearer mock-token")
                .header("Content-Type", "application/json")
                .when().log().all().post("/accounts")
                .then().log().all().statusCode(201)
                .extract().as(AccountModel.class);

        ValidatableResponse response = RestAssured.given()
                .header("Authorization", "Bearer mock-token")
                .when().log().all().get("/customer/accounts")
                .then().log().all().statusCode(200);

        softly.assertThat(response.body("id", Matchers.hasItem(newAcc.getId())));

    }

    @Test @Disabled
    @Tag("mock")
    @Tag("wiremock")
    public void authorizedUserCanSeeHisAccountsWithMock() {
        AccountModel newAcc = RestAssured.given()
                .header("Authorization", "Bearer mock-token")
                .header("Content-Type", "application/json")
                .when().log().all().post("/accounts")
                .then().log().all().statusCode(201)
                .extract().as(AccountModel.class);

        List<AccountModel> accounts = List.of(RestAssured.given()
                .header("Authorization", "Bearer mock-token")
                .log().all()
                .when().log().all().get("/customer/accounts")
                .then().log().all().statusCode(200)
                .extract().as(AccountModel[].class));

        AccountModel firstAcc = accounts.getFirst();

        ModelAssertions.assertThatModel(newAcc, firstAcc).match();
    }

}