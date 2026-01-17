package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.UserCleanupRegistry;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;

import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createNewUser(UserCleanupRegistry cleanupRegistry) {
        return StepLogger.log("createNewUser", () -> {
            CreateUserRequest createUserRequest =
                    RandomModelGenerator.generate(CreateUserRequest.class);

            ProfileModel profile = new ValidatedCrudRequester<ProfileModel>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_CREATE_USER,
                    ResponseSpecs.entityWasCreated())
                    .post(createUserRequest);

            if (cleanupRegistry != null) {
                cleanupRegistry.addUserForCleanup(profile);
            }

            return createUserRequest;
        });
    }

    public static ProfileModel createUser(CreateUserRequest createUserRequest) {
        return StepLogger.log("createUser", () -> {
            return new ValidatedCrudRequester<ProfileModel>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_CREATE_USER,
                    ResponseSpecs.entityWasCreated())
                    .post(createUserRequest);
        });
    }

    public static ValidatableResponse deleteUser(ProfileModel profileModel) {
        return StepLogger.log("deleteUser", () -> {
            return new CrudRequester(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_DELETE_USER,
                    ResponseSpecs.requestReturnsOK())
                    .delete(profileModel.getId());
        });
    }

    public static List<ProfileModel> getAllUsers() {
        return StepLogger.log("getAllUsers", () -> {
            return new ValidatedCrudRequester<ProfileModel>(RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_GET_USERS_LIST,
                    ResponseSpecs.requestReturnsOK()).getAll(ProfileModel[].class);
        });
    }
}
