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

public class TransferTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferBetweenOwnAccountsTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "usrFTransfer",
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
                                  "username": "usrFTransfer",
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

        Integer senderAccount = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
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
                                          "balance": 2000.0
                                        }
                                """.formatted(senderAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "senderAccountId": %d,
                                  "receiverAccountId": %d,
                                  "amount": 999.0
                                }
                                """.formatted(senderAccount, receiverAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"))
                .body("senderAccountId", Matchers.equalTo(senderAccount))
                .body("receiverAccountId", Matchers.equalTo(receiverAccount))
                .body("amount", Matchers.equalTo((float) 999.0));
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanSeeTransactionsTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "usrForSeeTrs",
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
                                  "username": "usrForSeeTrs",
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

        Integer senderAccount = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
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
                                          "balance": 2000.0
                                        }
                                """.formatted(senderAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "senderAccountId": %d,
                                  "receiverAccountId": %d,
                                  "amount": 999.0
                                }
                                """.formatted(senderAccount, receiverAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccount + "/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.containsString("DEPOSIT"))
                .body(Matchers.containsString("TRANSFER_OUT"));
    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferToOtherUserAccountTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                        {
                                          "username": "userForTr_2",
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
                                          "username": "userForTr_3",
                                          "password": "verysTRongPassword33$",
                                          "role": "USER"
                                        }
                                """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String authTokenUser2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                        {
                                          "username": "userForTr_2",
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

        String authTokenUser3 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                        {
                                          "username": "userForTr_3",
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

        Integer senderAccount = given()
                .header("Authorization", authTokenUser2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
                .header("Authorization", authTokenUser3)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", authTokenUser2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                        {
                                          "id": %d,
                                          "balance": 2000.0
                                        }
                                """.formatted(senderAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", authTokenUser2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                        {
                                          "senderAccountId": %d,
                                          "receiverAccountId": %d,
                                          "amount": 999.0
                                        }
                                """.formatted(senderAccount, receiverAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"))
                .body("senderAccountId", Matchers.equalTo(senderAccount))
                .body("receiverAccountId", Matchers.equalTo(receiverAccount))
                .body("amount", Matchers.equalTo((float) 999.0));
    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotTransferMoreThanBalanceTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                        {
                                          "username": "userForTrMtB",
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
                                          "username": "userForTrMtB",
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

        Integer senderAccount = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
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
                                          "balance": 500.0
                                        }
                                """.formatted(senderAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                        {
                                          "senderAccountId": %d,
                                          "receiverAccountId": %d,
                                          "amount": 1000.0
                                        }
                                """.formatted(senderAccount, receiverAccount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @Tag("POSITIVE")
    @MethodSource("validTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanTransferWithValidAmountsTest(String username, double amount) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                        {
                                          "username": "%s",
                                          "password": "verysTRongPassword33$",
                                          "role": "USER"
                                        }
                                """.formatted(username))
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
                                          "username": "%s",
                                          "password": "verysTRongPassword33$"
                                        }
                                """.formatted(username)
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        Integer senderAccount = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
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
                                          "balance": %.2f
                                        }
                                """.formatted(senderAccount, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String requestBody = String.format(
                """
                                {
                                  "senderAccountId": %d,
                                  "receiverAccountId": %d,
                                  "amount": %.2f
                                }
                        """, senderAccount, receiverAccount, amount);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"))
                .body("senderAccountId", Matchers.equalTo(senderAccount))
                .body("receiverAccountId", Matchers.equalTo(receiverAccount))
                .body("amount", Matchers.equalTo((float) amount));
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotTransferWithInvalidAmountsTest(String username, double amount, String errorValue) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                        {
                                          "username": "%s",
                                          "password": "verysTRongPassword33$",
                                          "role": "USER"
                                        }
                                """.formatted(username))
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
                                          "username": "%s",
                                          "password": "verysTRongPassword33$"
                                        }
                                """.formatted(username)
                )
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        Integer senderAccount = given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccount = given()
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
                                          "balance": 500.0
                                        }
                                """.formatted(senderAccount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String requestBody = String.format(
                """
                                {
                                  "senderAccountId": %d,
                                  "receiverAccountId": %d,
                                  "amount": %.2f
                                }
                        """, senderAccount, receiverAccount, amount);

        given()
                .header("Authorization", authToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));
    }

    public static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of("user_Tr_01", 1.0),
                Arguments.of("user_Tr_02", 1.1),
                Arguments.of("user_Tr_03", 4999.9),
                Arguments.of("user_Tr_04", 5000.0)
        );
    }

    public static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of("user_Tr_05", 0.0, "Transfer amount must be at least 0.01"),
                Arguments.of("user_Tr_06", -100.0, "Transfer amount must be at least 0.01"),
                Arguments.of("user_Tr_07", 10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of("user_Tr_08", -0.1, "Transfer amount must be at least 0.01")
        );
    }
}
