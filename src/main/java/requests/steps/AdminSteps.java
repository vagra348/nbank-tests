package requests.steps;

import generators.RandomModelGenerator;
import io.restassured.response.ValidatableResponse;
import models.CreateUserRequest;
import models.ProfileModel;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {

    public static CreateUserRequest createNewUser() {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        ProfileModel profile = new ValidatedCrudRequester<ProfileModel>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        return createUserRequest;
    }

    public static CreateUserRequest createUser(CreateUserRequest createUserRequest) {
        new ValidatedCrudRequester<ProfileModel>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        return createUserRequest;
    }

    public static ValidatableResponse deleteUser(ProfileModel profileModel) {
        return new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE_USER,
                ResponseSpecs.requestReturnsOK())
                .delete(profileModel.getId());
    }
}
