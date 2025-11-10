package iteration1;

import base.BaseTest;
import enums.ErrorText;
import enums.UserRole;
import generators.RandomData;
import models.CreateUserRequest;
import models.ProfileModel;
import models.comparison.ModelAssertions;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
                Arguments.of("admin", "Pass9Sym$", String.valueOf(UserRole.USER), "username", "Username 'admin' already exists"), //здесь нужен отдельный тест, т.к. ошибка не в JSON приходит
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
