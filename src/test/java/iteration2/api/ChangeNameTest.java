package iteration2.api;

import api.enums.ErrorText;
import api.generators.RandomModelGenerator;
import api.models.ChangeNameRequest;
import api.models.ChangeNameResponse;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanChangeNameTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

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
    @Tag("api")
    public void authorizedUserCanNotChangeNameWithInvalidDataTest(String name, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

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
