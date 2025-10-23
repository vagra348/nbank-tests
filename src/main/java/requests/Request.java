package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class Request<T extends BaseModel> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.responseSpecification = responseSpecification;
        this.requestSpecification = requestSpecification;
    }

    public abstract ValidatableResponse post(T model);

    public abstract ValidatableResponse post();

    public abstract ValidatableResponse get();

    public abstract ValidatableResponse get(Integer id);

    public abstract ValidatableResponse put(T model);

}
