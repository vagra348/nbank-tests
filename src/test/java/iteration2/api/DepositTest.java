package iteration2.api;

import base.BaseTest;
import enums.ErrorText;
import generators.RandomData;
import models.AccountModel;
import models.CreateUserRequest;
import models.MakeDepositRequest;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.List;
import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    @Tag("POSITIVE")
    @MethodSource("validDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanMakeDepositWithValidAmountsTest(double amount) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(newAcc, amount);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel firstAcc = accounts.getFirst();

        softly.assertThat(firstAcc.getBalance()).isEqualTo(amount);
        ModelAssertions.assertThatModel(firstAcc, newAcc).match();
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidDepositAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotMakeDepositWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(newAcc, amount);

        UserSteps.makeBadReqDeposit(createUserRequest, makeDepositRequest, errorValue);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel firstAcc = accounts.getFirst();

        ModelAssertions.assertThatModel(firstAcc, newAcc).match();
    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToOtherUserAccountTest() {
        CreateUserRequest user1Request = AdminSteps.createNewUser(this);

        CreateUserRequest user2Request = AdminSteps.createNewUser(this);

        AccountModel accountUser2 = UserSteps.createAccount(user2Request);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(accountUser2, 0.01, 5000.0);

        UserSteps.makeForbiddenDeposit(user1Request, makeDepositRequest);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(user2Request)
                .extract().as(AccountModel[].class));

        AccountModel firstAcc = accounts.getFirst();

        softly.assertThat(firstAcc.getBalance()).isEqualTo(accountUser2.getBalance());
        ModelAssertions.assertThatModel(firstAcc, accountUser2).match();
    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotMakeDepositToNonExistAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

        Integer nonExistentAccountId = newAcc.getId() + 1000;

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(nonExistentAccountId)
                .balance(RandomData.generateSum(0.01, 5000.0))
                .build();

        UserSteps.makeForbiddenDeposit(createUserRequest, makeDepositRequest);
    }

    public static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(0.02),
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