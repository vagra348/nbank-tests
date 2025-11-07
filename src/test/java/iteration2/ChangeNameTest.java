package iteration2;

import base.BaseTest;
import enums.ErrorText;
import generators.RandomModelGenerator;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import models.CreateUserRequest;
import models.ProfileModel;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanChangeNameTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        ChangeNameRequest changeNameRequest = RandomModelGenerator.generate(ChangeNameRequest.class);

        ChangeNameResponse changeNameResponse = UserSteps.changeName(createUserRequest, changeNameRequest);

        softly.assertThat(changeNameResponse.getMessage()).isEqualTo("Profile updated successfully");

        ModelAssertions.assertThatModel(changeNameResponse.getCustomer(), changeNameRequest).match();

        ProfileModel profile = UserSteps.getProfile(createUserRequest);

        ModelAssertions.assertThatModel(createUserRequest, profile).match();
        ModelAssertions.assertThatModel(profile, changeNameRequest).match();

    }

    @Tag("NEGATIVE")
    @MethodSource("invalidNameData")
    @ParameterizedTest
    public void authorizedUserCanNotChangeNameWithInvalidDataTest(String name, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder().name(name).build();

        UserSteps.changeNameBadReq(createUserRequest, changeNameRequest, errorValue);

        ProfileModel profile = UserSteps.getProfile(createUserRequest);

        ModelAssertions.assertThatModel(createUserRequest, profile).match();
        softly.assertThat(profile.getName()).isEqualTo(null);
    }

    public static Stream<Arguments> invalidNameData() {
        return Stream.of(
                Arguments.of("", ErrorText.invalidName.getTitle()),
                Arguments.of("John", ErrorText.invalidName.getTitle()),
                Arguments.of("John Smith Third", ErrorText.invalidName.getTitle()),
                Arguments.of("John123 Smith", ErrorText.invalidName.getTitle()),
                Arguments.of("John Smith!", ErrorText.invalidName.getTitle()),
                Arguments.of("John Смит", ErrorText.invalidName.getTitle()),
                Arguments.of("John_Smith", ErrorText.invalidName.getTitle()),
                Arguments.of("John-Smith", ErrorText.invalidName.getTitle())
        );
    }
}
