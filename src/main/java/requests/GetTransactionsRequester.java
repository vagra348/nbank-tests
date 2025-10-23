package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class GetTransactionsRequester extends Request<BaseModel> {
    public GetTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse post() {
        return null;
    }

    @Override
    public ValidatableResponse get(Integer id) {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/accounts/" + id.toString() + "/transactions")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return null;
    }
}
