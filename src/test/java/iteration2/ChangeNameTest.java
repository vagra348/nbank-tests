package iteration2;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.GetUserProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanChangeNameTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(RandomData.qenerateName())
                .build();

        ChangeNameResponse changeNameResponse = new ChangeNameRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(changeNameRequest)
                .extract().as(ChangeNameResponse.class);

        softly.assertThat(changeNameResponse.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(changeNameResponse.getCustomer().getName()).isEqualTo(changeNameRequest.getName());

        ProfileModel profile = new GetUserProfileRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get().extract().as(ProfileModel.class);

        softly.assertThat(profile.getUsername()).isEqualTo(createUserRequest.getUsername());
        softly.assertThat(profile.getName()).isEqualTo(changeNameRequest.getName());


    }

    @Tag("NEGATIVE")
    @MethodSource("invalidNameData")
    @ParameterizedTest
    public void authorizedUserCanNotChangeNameWithInvalidDataTest(String name, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(name)
                .build();

        new ChangeNameRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(errorValue))
                .put(changeNameRequest);

        ProfileModel profile = new GetUserProfileRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(ProfileModel.class);

        softly.assertThat(profile.getUsername()).isEqualTo(createUserRequest.getUsername());
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
