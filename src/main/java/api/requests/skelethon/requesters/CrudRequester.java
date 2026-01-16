package api.requests.skelethon.requesters;

import api.configs.Config;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import api.requests.skelethon.interfaces.GetAllEndpoindInterface;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpoindInterface {
    private final static String API_V = Config.getProperty("apiVersion");

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint,
                         ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_V + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_V + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get(Integer id) {
        String url = API_V + endpoint.buildUrlWithPathParam(id);

        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(url)
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get(String param, Integer id) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .pathParam(param, id)
                    .get(API_V + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        var body = model == null ? "" : model;
        return StepLogger.log("PUT request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .put(API_V + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse delete(Integer id) {
        String url = API_V + endpoint.buildUrlWithPathParam(id);
        return StepLogger.log("DELETE request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .delete(url)
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_V + endpoint.getUrl())
                    .then().assertThat()
                    .spec(responseSpecification);
        });
    }
}
