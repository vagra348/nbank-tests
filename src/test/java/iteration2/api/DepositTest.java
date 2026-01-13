package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.database.Condition;
import api.database.DBRequest;
import api.enums.ErrorText;
import api.generators.RandomData;
import api.models.AccountModel;
import api.models.CreateUserRequest;
import api.models.MakeDepositRequest;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import base.BaseTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseTest {

    @Tag("POSITIVE")
    @MethodSource("validDepositAmounts")
    @ParameterizedTest
    @Tag("api")
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

        // БД-проверка, что баланс поменялся
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(newAcc.getAccountNumber());
        DaoAndModelAssertions.assertThat(firstAcc, accountDao).match();
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidDepositAmounts")
    @ParameterizedTest
    @Tag("api")
    public void authorizedUserCanNotMakeDepositWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(newAcc, amount);

        UserSteps.makeBadReqDeposit(createUserRequest, makeDepositRequest, errorValue);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel firstAcc = accounts.getFirst();

        ModelAssertions.assertThatModel(firstAcc, newAcc).match();

        // БД-проверка, что баланс не поменялся
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(newAcc.getAccountNumber());
        DaoAndModelAssertions.assertThat(newAcc, accountDao).match();
    }

    @Tag("NEGATIVE")
    @Test
    @Tag("api")
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

        // БД-проверка, что баланс у юзера 2 не поменялся
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountUser2.getAccountNumber());
        DaoAndModelAssertions.assertThat(firstAcc, accountDao).match();
    }

    @Tag("NEGATIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanNotMakeDepositToNonExistAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

        Integer nonExistentAccountId = newAcc.getId() + 1000;

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(nonExistentAccountId)
                .balance(RandomData.generateSum(0.01, 5000.0))
                .build();

        UserSteps.makeForbiddenDeposit(createUserRequest, makeDepositRequest);

        // БД-проверка, что не существует (некуда отправлять) или каким-то образом не создался новый аккаунт
        String sqlResponse = DBRequest.builder()
                .requestType(DBRequest.RequestType.SELECT_AND)
                .table(DataBaseSteps.DBTables.ACCOUNTS.getName())
                .where(Condition.equalTo("id", nonExistentAccountId))
                .extractAs(String.class);
        assertThat(sqlResponse).isNull();
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