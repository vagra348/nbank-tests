package iteration2;

import base.BaseTest;
import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import models.CreateUserRequest;
import models.ErrorText;
import models.MakeDepositRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetUserAccountsRequester;
import requests.MakeDepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    @Tag("POSITIVE")
    @MethodSource("validDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanMakeDepositWithValidAmountsTest(double amount) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        Integer accountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest)
                .assertThat()
                .body("id", Matchers.equalTo(accountId))
                .body("balance", Matchers.equalTo((float)amount));


        ValidatableResponse response = new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get();

        softly.assertThat(response.body("id", Matchers.hasItem(accountId)));
        softly.assertThat(response.body("balance", Matchers.hasItem((float)amount)));
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotMakeDepositWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        Integer accountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(errorValue))
                .post(makeDepositRequest);
    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToOtherUserAccountTest() {
        CreateUserRequest user1Request = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(user1Request);

        CreateUserRequest user2Request = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(user2Request);

        Integer accountIdUser2 = new CreateAccountRequester(
                RequestSpecs.authUserSpec(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(accountIdUser2)
                .balance(1000.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestForbidden())
                .post(makeDepositRequest);
    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToNonExistAccountTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        Integer realAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer nonExistentAccountId = realAccountId + 1000;

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(nonExistentAccountId)
                .balance(1000.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestForbidden())
                .post(makeDepositRequest);
    }

    public static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(1.0),
                Arguments.of(1.1),
                Arguments.of(4999.9),
                Arguments.of(5000.0)
        );
    }

    public static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(0.0, ErrorText.invalidDepositAmount.getTitle()),
                Arguments.of(-0.1, ErrorText.invalidDepositAmount.getTitle()),
                Arguments.of(-100.0, ErrorText.invalidDepositAmount.getTitle()),
                Arguments.of(5000.01, ErrorText.hugeDepositAmountError.getTitle()),
                Arguments.of(10000.0, ErrorText.hugeDepositAmountError.getTitle())
        );
    }
}