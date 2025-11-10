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
import utils.UserCleanupRegistry;

public class AdminSteps {

    public static CreateUserRequest createNewUser(UserCleanupRegistry cleanupRegistry) {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        ProfileModel profile = new ValidatedCrudRequester<ProfileModel>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        cleanupRegistry.addUserForCleanup(profile);

        return createUserRequest;
    }


    public static ProfileModel createUser(CreateUserRequest createUserRequest) {
        return new ValidatedCrudRequester<ProfileModel>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);
    }

    public static ValidatableResponse deleteUser(ProfileModel profileModel) {
        return new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_DELETE_USER,
                ResponseSpecs.requestReturnsOK())
                .delete(profileModel.getId());
    }
}
