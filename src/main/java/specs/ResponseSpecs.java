package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import models.ErrorText;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs(){}

    private static ResponseSpecBuilder defaultResponseBuilder(){
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification badRequest(String errorKey, String errorValue){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.hasItem(errorValue))
                .build();
    }

    public static ResponseSpecification badRequest(String errorValue){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(errorValue))
                .build();
    }

    public static ResponseSpecification requestUnauthorized(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody("error", Matchers.equalTo(ErrorText.invalidAuthData.getTitle()))
                .build();
    }


    public static ResponseSpecification requestForbidden(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.equalTo(ErrorText.unauthAccessError.getTitle()))
                .build();
    }
}
