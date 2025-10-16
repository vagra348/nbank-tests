package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class LoginUserTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    @Tag("POSITIVE")
    public void adminCanGenerateAuthTokenTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "admin",
                                  "password": "admin"
                                }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=");
    }

    @Test
    @Tag("POSITIVE")
    public void userCanGenerateAuthTokenTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "newCoolUser",
                                  "password": "verysTRongPassword33$",
                                  "role": "USER"
                                }
                                """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "newCoolUser",
                                  "password": "verysTRongPassword33$"
                                }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", Matchers.notNullValue());
    }

    @Test
    @Tag("POSITIVE")
    public void authorizedAdminCanSeeUsersListTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "newCoolUser2",
                                  "password": "verysTRongPassword33$",
                                  "role": "USER"
                                }
                                """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .get("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.containsString("newCoolUser2"));
    }

    @Test
    @Tag("NEGATIVE")
    public void nonExistLoginTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "userThatDoesNotExist",
                                  "password": "admin"
                                }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error", Matchers.equalTo("Invalid username or password"));
    }

    @Test
    @Tag("NEGATIVE")
    public void incorrectPasswordTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "admin",
                                  "password": "adminIncorrectPass"
                                }
                                """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error", Matchers.equalTo("Invalid username or password"));
    }

}
