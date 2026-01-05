package iteration1.api;

import api.configs.Config;
import api.enums.ErrorText;
import api.enums.UserRole;
import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.ProfileModel;
import api.models.comparison.ModelAssertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Tag("POSITIVE")
    @MethodSource("userCorrectData")
    @ParameterizedTest
    public void adminCanCreateUserWithCorrectDataTest(String username, String password, String role) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        ProfileModel createUserResponse = AdminSteps.createUser(createUserRequest);

        addUserForCleanup(createUserResponse);

        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        ModelAssertions.assertThatModel(createUserRequest, createUserResponse).match();
    }

    @Tag("NEGATIVE")
    @Test
    public void adminCanNotCreateUserWithExistingUsername() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(Config.getProperty("admin.username"))
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        String response = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.badRequest())
                .post(createUserRequest)
                .extract().body().asString();

        softly.assertThat(response).isEqualTo("Error: Username '" + Config.getProperty("admin.username") + "' already exists.");
    }

    @Tag("NEGATIVE")
    @MethodSource("usernameInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidUsername(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        String response = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.badRequest())
                .post(createUserRequest)
                .extract().body().asString();

        softly.assertThat(response).contains(errorKey);
        softly.assertThat(response).contains(errorValue);
    }

    @Tag("NEGATIVE")
    @MethodSource("passwordInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidPassword(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        String response = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.badRequest())
                .post(createUserRequest)
                .extract().body().asString();

        softly.assertThat(response).contains(errorKey);
        softly.assertThat(response).contains(errorValue);
    }

    @Tag("NEGATIVE")
    @MethodSource("roleInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidRole(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        String response = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_CREATE_USER,
                ResponseSpecs.badRequest())
                .post(createUserRequest)
                .extract().body().asString();

        softly.assertThat(response).contains(errorKey);
        softly.assertThat(response).contains(errorValue);

    }


    public static Stream<Arguments> userCorrectData() {
        return Stream.of(
                Arguments.of(RandomStringUtils.randomAlphanumeric(15), "Test12345$", String.valueOf(UserRole.USER)),
                Arguments.of(RandomStringUtils.randomAlphanumeric(3), "Test12345$", "ADMIN"),
                Arguments.of(RandomStringUtils.randomAlphanumeric(8), "Pas8Sym$", String.valueOf(UserRole.USER))
        );
    }

    public static Stream<Arguments> usernameInvalidData() {
        return Stream.of(
                Arguments.of("", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.blankUsernameError.getTitle()),
                Arguments.of("   ", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.blankUsernameError.getTitle()),
                Arguments.of("ab", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameLengthError.getTitle()),
                Arguments.of("abcdefgh12345678", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameLengthError.getTitle()),
                Arguments.of("кириллица", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()), //в требования не было, но наверняка подразумевалось - нужно уточнять
                Arguments.of("ab c", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12$", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12@", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12#", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12%", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12^", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12*", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12&", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12!", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12\"", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12`", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12~", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12<", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12>", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12?", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12/", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12\\", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12|", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12,", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12+", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12:", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12{", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12}", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12[", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12]", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12(", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle()),
                Arguments.of("ab12)", "Test12345$", String.valueOf(UserRole.USER), "username", ErrorText.usernameExtraSymbolError.getTitle())
        );
    }

    public static Stream<Arguments> passwordInvalidData() {
        return Stream.of(
                Arguments.of(RandomData.qenerateUsername(), "", String.valueOf(UserRole.USER), "password", ErrorText.blankPasswordError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "         ", String.valueOf(UserRole.USER), "password", ErrorText.blankPasswordError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "тестПароль№1", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "tstPs1$", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "testPass 123$", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "testPass123", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "testpass123$", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "TESTPASS123$", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "testPass$", String.valueOf(UserRole.USER), "password", ErrorText.passwordValidationError.getTitle())
        );
    }

    public static Stream<Arguments> roleInvalidData() {
        return Stream.of(
                Arguments.of(RandomData.qenerateUsername(), "Test12345$", "", "role", ErrorText.roleValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "Test12345$", "User", "role", ErrorText.roleValidationError.getTitle()),
                Arguments.of(RandomData.qenerateUsername(), "Test12345$", "SMBDY", "role", ErrorText.roleValidationError.getTitle())
        );
    }

}
