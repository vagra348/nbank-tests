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

public class DepositTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Tag("POSITIVE")
    @MethodSource("validDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanMakeDepositWithValidAmountsTest(String username, double amount) {
        String adminCreateUserRequestBody =
                String.format("""
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

        Integer accountId = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String requestBody = String.format(
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                 """, accountId, amount);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(accountId))
                .body("balance", Matchers.equalTo((float)amount));
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotMakeDepositWithInvalidAmountsTest(String username, double amount, String errorValue) {
        String adminCreateUserRequestBody =
                String.format("""
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

        Integer accountId = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        String requestBody = String.format(
                """
                        {
                          "id": %d,
                          "balance": %.2f
                        }
                        """, accountId, amount);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }


    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToOtherUserAccountTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "user_01Dep",
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
                .body(
                        """
                                {
                                  "username": "user_02Dep",
                                  "password": "verysTRongPassword33$",
                                  "role": "USER"
                                }
                                """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String authTokenUser1 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "user_01Dep",
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

        String authTokenUser2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "user_02Dep",
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

        Integer accountIdUser2 = given()
                .header("Authorization", authTokenUser2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", authTokenUser1)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "id": %d,
                                  "balance": 1000.0
                                }
                                """.formatted(accountIdUser2))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }


    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToNonExistAccountTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "uDNoExAcc",
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
                                  "username": "uDNoExAcc",
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

        Integer accountId = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "id": %d,
                                  "balance": 1000.0
                                }
                                """.formatted(accountId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account")); //здесь было бы логичнее другое сообщение об ошибке, но можно и такую логику понять тоже
    }

    public static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of("user_Depos01", 1.0),
                Arguments.of("user_Depos02", 1.1),
                Arguments.of("user_Depos03", 4999.9),
                Arguments.of("user_Depos04", 5000.0)
        );
    }

    public static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of("user_Depos05", 0.0, "Deposit amount must be at least 0.01"),
                Arguments.of("user_Depos06", -0.1, "Deposit amount must be at least 0.01"),
                Arguments.of("user_Depos07", -100.0, "Deposit amount must be at least 0.01"),
                Arguments.of("user_Depos08", 5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of("user_Depos09", 10000.0, "Deposit amount cannot exceed 5000")
        );
    }
}
