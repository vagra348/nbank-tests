package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Tag("POSITIVE")
    @MethodSource("userCorrectData")
    @ParameterizedTest
    public void adminCanCreateUserWithCorrectDataTest(String username, String password, String role) {
        String requestBody = String.format(
                """
                        {
                              "username": "%s",
                              "password": "%s",
                              "role": "%s"
                        }
                        """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo(username))
                .body("password", Matchers.not(Matchers.equalTo(password)))
                .body("role", Matchers.equalTo(role));
    }

    @Tag("NEGATIVE")
    @MethodSource("usernameInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidUsername(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format(
                """
                        {
                              "username": "%s",
                              "password": "%s",
                              "role": "%s"
                        }
                        """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.hasItem(errorValue));
    }

    @Tag("NEGATIVE")
    @MethodSource("passwordInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidPassword(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format(
                """
                        {
                              "username": "%s",
                              "password": "%s",
                              "role": "%s"
                        }
                        """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.hasItem(errorValue));
    }

    @Tag("NEGATIVE")
    @MethodSource("roleInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidRole(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format(
                """
                        {
                              "username": "%s",
                              "password": "%s",
                              "role": "%s"
                        }
                        """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.hasItem(errorValue));
    }


    public static Stream<Arguments> userCorrectData() {
        return Stream.of(
                Arguments.of("A.n-Na_15Symbls", "Test12345$", "USER"),
                Arguments.of("Ad3", "Test12345$", "ADMIN"),
                Arguments.of("A.N-na_0", "Pas8Sym$", "USER")
        );
    }

    public static Stream<Arguments> usernameInvalidData() {
        return Stream.of(
                Arguments.of("admin", "Pass9Sym$", "USER", "username", "Username 'admin' already exists"), //здесь нужен отдельный тест, т.к. ошибка не в JSON приходит
                Arguments.of("", "Test12345$", "USER", "username", "Username cannot be blank"),
                Arguments.of("   ", "Test12345$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Test12345$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abcdefgh12345678", "Test12345$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("кириллица", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"), //в требования не было, но наверняка подразумевалось - нужно уточнять
                Arguments.of("ab c", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12$", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12@", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12#", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12%", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12^", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12*", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12&", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12!", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12\"", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12`", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12~", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12<", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12>", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12?", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12/", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12\\", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12|", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12,", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12+", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12:", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12{", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12}", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12[", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12]", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12(", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("ab12)", "Test12345$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    public static Stream<Arguments> passwordInvalidData() {
        return Stream.of(
                Arguments.of("aaa1", "", "USER", "password", "Password cannot be blank"),
                Arguments.of("aaa1", "         ", "USER", "password", "Password cannot be blank"),
                Arguments.of("aaa1", "тестПароль№1", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("aaa1", "tstPs1$", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("aaa1", "testPass 123$", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("aaa1", "testPass123", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("bbb2", "testpass123$", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("ccc3", "TESTPASS123$", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"),
                Arguments.of("ddd4", "testPass$", "USER", "password", "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long")
        );
    }

    public static Stream<Arguments> roleInvalidData() {
        return Stream.of(
                Arguments.of("user1", "Test12345$", "", "role", "Role must be either 'ADMIN' or 'USER'"),
                Arguments.of("user2", "Test12345$", "User", "role", "Role must be either 'ADMIN' or 'USER'"),
                Arguments.of("smbdy", "Test12345$", "SMBDY", "role", "Role must be either 'ADMIN' or 'USER'")
        );
    }

}
