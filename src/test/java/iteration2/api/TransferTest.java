package iteration2.api;

import api.enums.ErrorText;
import api.enums.TransactionType;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static api.specs.RequestSpecs.getUserAuthHeader;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.offset;

public class TransferTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanTransferBetweenOwnAccountsTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, 1.0, 1999.0);

        TransferResponse transferResponse = UserSteps.makeTransfer(createUserRequest, transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        ModelAssertions.assertThatModel(transferResponse, transferRequest).match();

        List<TransactionModel> transactionsSenderAcc = List.of(UserSteps.getTransactions(createUserRequest, senderAccount)
                .extract().as(TransactionModel[].class));

        TransactionModel targetOutTransaction = transactionsSenderAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));

        List<TransactionModel> transactionsReceiverAcc = List.of(UserSteps.getTransactions(createUserRequest, receiverAccount)
                .extract().as(TransactionModel[].class));

        TransactionModel targetInTransaction = transactionsReceiverAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        ModelAssertions.assertThatModel(targetInTransaction, transferRequest).match();
        ModelAssertions.assertThatModel(targetOutTransaction, transferRequest).match();

        softly.assertThat(targetInTransaction.getType()).isEqualTo(String.valueOf(TransactionType.TRANSFER_IN));
        softly.assertThat(targetOutTransaction.getType()).isEqualTo(String.valueOf(TransactionType.TRANSFER_OUT));

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));


        softly.assertThat(changedSenderAcc.getBalance())
                .isCloseTo(makeDepositRequest.getBalance() - transferRequest.getAmount(), offset(0.01));
        softly.assertThat(changedReceiverAcc.getBalance())
                .isCloseTo(transferRequest.getAmount(), offset(0.01));


        // БД-проверка появившихся в истории транзакций
//        TransactionDao transactionDao = DataBaseSteps.getTransferByAccountId(transferRequest.getReceiverAccountId());
//        DaoAndModelAssertions.assertThat(transferResponse, transactionDao).match();
//
//        TransactionDao transactionInDao = DataBaseSteps.getTransferById(targetInTransaction.getId());
//        softly.assertThat(transactionInDao.getType()).isEqualTo(TransactionType.TRANSFER_IN.toString());
//        TransactionDao transactionOutDao = DataBaseSteps.getTransferById(targetOutTransaction.getId());
//        softly.assertThat(transactionOutDao.getType()).isEqualTo(TransactionType.TRANSFER_OUT.toString());

        // БД-проверка, что баланс двух счетов такой, какой ожидаем
//        AccountDao senderAccDao = DataBaseSteps.getAccountByAccountId(senderAccount.getId());
//        AccountDao receiverAccDao = DataBaseSteps.getAccountByAccountId(receiverAccount.getId());
//
//        softly.assertThat(senderAccDao.getBalance())
//                .isCloseTo(makeDepositRequest.getBalance() - transferRequest.getAmount(), offset(0.01));
//        softly.assertThat(receiverAccDao.getBalance())
//                .isCloseTo(transferRequest.getAmount(), offset(0.01));
    }

    @Tag("POSITIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanSeeTransactionsTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, 1.0, 1999.0);

        UserSteps.makeTransfer(createUserRequest, transferRequest);

        List<TransactionModel> transactions = List.of(UserSteps.getTransactions(createUserRequest, senderAccount)
                .extract().as(TransactionModel[].class));

        TransactionModel targetDepositTransaction = transactions.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        TransactionModel targetOutTransaction = transactions.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));

        ModelAssertions.assertThatModel(makeDepositRequest, targetDepositTransaction).match();
        ModelAssertions.assertThatModel(targetOutTransaction, transferRequest).match();

        softly.assertThat(targetDepositTransaction.getType()).isEqualTo(String.valueOf(TransactionType.DEPOSIT));
        softly.assertThat(targetOutTransaction.getType()).isEqualTo(String.valueOf(TransactionType.TRANSFER_OUT));


        // БД-проверка количества появившихся в истории транзакций, можно ещё добавить, как в пред.тесте - на соотв-ие amount
//        List<TransactionDao> transactionsList = DBRequest.builder()
//                .requestType(DBRequest.RequestType.SELECT_OR)
//                .table(DataBaseSteps.DBTables.TRANSACTIONS.getName())
//                .where(Condition.equalTo(DataBaseSteps.DBTables.TRANSACTIONS_RELATED_ACC_ID.getName(), senderAccount.getId()))
//                .where(Condition.equalTo(DataBaseSteps.DBTables.TRANSACTIONS_RELATED_ACC_ID.getName(), receiverAccount.getId()))
//                .where(Condition.equalTo(DataBaseSteps.DBTables.TRANSACTIONS_ACC_ID.getName(), senderAccount.getId()))
//                .where(Condition.equalTo(DataBaseSteps.DBTables.TRANSACTIONS_ACC_ID.getName(), receiverAccount.getId()))
//                .extractAsList(TransactionDao.class);
//        softly.assertThat(transactionsList.size()).isEqualTo(3);
    }

    @Tag("POSITIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanTransferToOtherUserAccountTest() {
        CreateUserRequest user1Request = AdminSteps.createNewUser(this);

        CreateUserRequest user2Request = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(user1Request);

        AccountModel receiverAccount = UserSteps.createAccount(user2Request);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(user1Request, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, 1.0, 1999.0);

        TransferResponse transferResponse = UserSteps.makeTransfer(user1Request, transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        ModelAssertions.assertThatModel(transferResponse, transferRequest).match();

        List<TransactionModel> transactionsSenderAcc = List.of(UserSteps.getTransactions(user1Request, senderAccount)
                .extract().as(TransactionModel[].class));

        TransactionModel targetOutTransaction = transactionsSenderAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));

        List<TransactionModel> transactionsReceiverAcc = List.of(UserSteps.getTransactions(user2Request, receiverAccount)
                .extract().as(TransactionModel[].class));

        TransactionModel targetInTransaction = transactionsReceiverAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        ModelAssertions.assertThatModel(targetInTransaction, transferRequest).match();
        ModelAssertions.assertThatModel(targetOutTransaction, transferRequest).match();

        softly.assertThat(targetInTransaction.getType()).isEqualTo(String.valueOf(TransactionType.TRANSFER_IN));
        softly.assertThat(targetOutTransaction.getType()).isEqualTo(String.valueOf(TransactionType.TRANSFER_OUT));

        List<AccountModel> accountsUser1 = List.of(UserSteps.getAccounts(user1Request)
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accountsUser1.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        List<AccountModel> accountsUser2 = List.of(UserSteps.getAccounts(user2Request)
                .extract().as(AccountModel[].class));

        AccountModel changedReceiverAcc = accountsUser2.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));


        softly.assertThat(changedSenderAcc.getBalance()).isCloseTo(makeDepositRequest.getBalance() - transferRequest.getAmount(), offset(0.01));
        softly.assertThat(changedReceiverAcc.getBalance()).isCloseTo(transferRequest.getAmount(), offset(0.01));


        // БД-проверка появившихся в истории транзакций
//        TransactionDao transactionDao = DataBaseSteps.getTransferByAccountId(transferRequest.getReceiverAccountId());
//        DaoAndModelAssertions.assertThat(transferResponse, transactionDao).match();
//
//        TransactionDao transactionInDao = DataBaseSteps.getTransferById(targetInTransaction.getId());
//        softly.assertThat(transactionInDao.getType()).isEqualTo(TransactionType.TRANSFER_IN.toString());
//        TransactionDao transactionOutDao = DataBaseSteps.getTransferById(targetOutTransaction.getId());
//        softly.assertThat(transactionOutDao.getType()).isEqualTo(TransactionType.TRANSFER_OUT.toString());

        // БД-проверка, что баланс двух счетов такой, какой ожидаем
//        AccountDao senderAccDao = DataBaseSteps.getAccountByAccountId(senderAccount.getId());
//        AccountDao receiverAccDao = DataBaseSteps.getAccountByAccountId(receiverAccount.getId());
//
//        softly.assertThat(senderAccDao.getBalance())
//                .isCloseTo(makeDepositRequest.getBalance() - transferRequest.getAmount(), offset(0.01));
//        softly.assertThat(receiverAccDao.getBalance())
//                .isCloseTo(transferRequest.getAmount(), offset(0.01));

    }

    @Tag("NEGATIVE")
    @Test
    @Tag("api")
    public void authorizedUserCanNotTransferMoreThanBalanceTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 1.0, 500.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, 1000.0, 5000.0);

        UserSteps.makeTransferBadReq(createUserRequest, transferRequest, ErrorText.invalidTransferError.getTitle());

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));

        softly.assertThat(changedSenderAcc.getBalance()).isCloseTo(makeDepositRequest.getBalance(), offset(0.01));
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(0.0);


        // БД-проверка, что баланс обоих счетов не поменялся
//        AccountDao senAccountDao = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());
//        DaoAndModelAssertions.assertThat(changedSenderAcc, senAccountDao).match();
//        AccountDao recAccountDao = DataBaseSteps.getAccountByAccountNumber(receiverAccount.getAccountNumber());
//        DaoAndModelAssertions.assertThat(changedReceiverAcc, recAccountDao).match();

    }

    @Tag("POSITIVE")
    @MethodSource("validTransferAmounts")
    @ParameterizedTest
    @Tag("api")
    public void authorizedUserCanTransferWithValidAmountsTest(double depositAmount, double transferAmount) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, transferAmount);

        UserSteps.makeTransfer(createUserRequest, transferRequest);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));


        softly.assertThat(changedSenderAcc.getBalance()).isCloseTo(makeDepositRequest.getBalance() * 2 - transferRequest.getAmount(), offset(0.01));
        softly.assertThat(changedReceiverAcc.getBalance()).isCloseTo(transferRequest.getAmount(), offset(0.01));


        // БД-проверка, что баланс двух счетов такой, какой ожидаем
//        AccountDao senderAccDao = DataBaseSteps.getAccountByAccountId(senderAccount.getId());
//        AccountDao receiverAccDao = DataBaseSteps.getAccountByAccountId(receiverAccount.getId());
//
//        softly.assertThat(senderAccDao.getBalance())
//                .isCloseTo(makeDepositRequest.getBalance() * 2 - transferRequest.getAmount(), offset(0.01));
//        softly.assertThat(receiverAccDao.getBalance())
//                .isCloseTo(transferRequest.getAmount(), offset(0.01));
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidTransferAmounts")
    @ParameterizedTest
    @Tag("api")
    public void authorizedUserCanNotTransferWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 0.01, 5000.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount, receiverAccount, amount);

        UserSteps.makeTransferBadReq(createUserRequest, transferRequest, errorValue);

        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));

        softly.assertThat(changedSenderAcc.getBalance()).isCloseTo(makeDepositRequest.getBalance(), offset(0.01));
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(0.0);


        // БД-проверка, что баланс обоих счетов не поменялся
//        AccountDao senAccountDao = DataBaseSteps.getAccountByAccountNumber(senderAccount.getAccountNumber());
//        DaoAndModelAssertions.assertThat(changedSenderAcc, senAccountDao).match();
//        AccountDao recAccountDao = DataBaseSteps.getAccountByAccountNumber(receiverAccount.getAccountNumber());
//        DaoAndModelAssertions.assertThat(changedReceiverAcc, recAccountDao).match();
    }


    @Tag("NEGATIVE")
    @Test
    @Tag("api")
    public void apiCanNotAcceptGetTransactionsWithoutId() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);
        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        given()
                .header("Authorization", getUserAuthHeader(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .pathParam("accountId",
                        "")
                .when()
                .get("/api/v1/accounts/{accountId}/transactions")
                .then()
                .statusCode(404);

    }

    public static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(0.01, 0.01),
                Arguments.of(0.02, 0.02),
                Arguments.of(5000.0, 9999.9),
                Arguments.of(5000.0, 10000.0)
        );
    }

    public static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(0.0, ErrorText.invalidTransferError.getTitle()),
                Arguments.of(-100.0, ErrorText.invalidTransferError.getTitle()),
                Arguments.of(10000.01, ErrorText.hugeTransferAmountError.getTitle()),
                Arguments.of(-0.1, ErrorText.invalidTransferError.getTitle())
        );
    }
}
