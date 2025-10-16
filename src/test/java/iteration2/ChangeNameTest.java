package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class ChangeNameTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanChangeNameTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "usrForNamCh",
                                  "password": "verysTRongPassword33$",
                                  "role": "USER"
                                }
                           """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String authToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "usrForNamCh",
                                  "password": "verysTRongPassword33$"
                                }
                           """
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "name": "One Two"
                                }
                           """
                )
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("One Two"))
                .body("message", Matchers.equalTo("Profile updated successfully"));
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidNameData")
    @ParameterizedTest
    public void authorizedUserCanNotChangeNameWithInvalidDataTest(String username, String name, String errorValue) {
        String adminCreateUserRequestBody = String.format("""
                                {
                                  "username": "%s",
                                  "password": "verysTRongPassword33$",
                                  "role": "USER"
                                }
                          """, username);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(adminCreateUserRequestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String userLoginRequestBody = String.format("""
                                {
                                  "username": "%s",
                                  "password": "verysTRongPassword33$"
                                }
                          """, username);
        String authToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(userLoginRequestBody)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        String requestBody = String.format(
                """
                        {
                          "name": "%s"
                        }
                 """, name);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }

    public static Stream<Arguments> invalidNameData() {
        return Stream.of(
                Arguments.of("username1", "", "Name must contain two words with letters only"),
                Arguments.of("username2", "John", "Name must contain two words with letters only"),
                Arguments.of("username3", "John Smith Third", "Name must contain two words with letters only"),
                Arguments.of("username4", "John123 Smith", "Name must contain two words with letters only"),
                Arguments.of("username5", "John Smith!", "Name must contain two words with letters only"),
                Arguments.of("username6", "John Смит", "Name must contain two words with letters only"),
                Arguments.of("username7", "John_Smith", "Name must contain two words with letters only"),
                Arguments.of("username8", "John-Smith", "Name must contain two words with letters only")
        );
    }
}
