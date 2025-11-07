package iteration2;

import base.BaseTest;
import enums.ErrorText;
import enums.TransactionType;
import generators.RandomModelGenerator;
import models.*;
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

public class TransferTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferBetweenOwnAccountsTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount,receiverAccount, 1.0, 1999.0);

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


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() - transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());

    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanSeeTransactionsTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount,receiverAccount, 1.0, 1999.0);

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

    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferToOtherUserAccountTest() {
        CreateUserRequest user1Request =
                RandomModelGenerator.generate(CreateUserRequest.class);

        AdminSteps.createUser(user1Request);
        ProfileModel userToDelete1 = UserSteps.getProfile(user1Request);
        addUserForCleanup(userToDelete1);

        CreateUserRequest user2Request =
                RandomModelGenerator.generate(CreateUserRequest.class);

        AdminSteps.createUser(user2Request);
        ProfileModel userToDelete2 = UserSteps.getProfile(user2Request);
        addUserForCleanup(userToDelete2);

        AccountModel senderAccount = UserSteps.createAccount(user1Request);

        AccountModel receiverAccount = UserSteps.createAccount(user2Request);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);

        UserSteps.makeDeposit(user1Request, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount,receiverAccount, 1.0, 1999.0);

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


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() - transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());

    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotTransferMoreThanBalanceTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);

        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 1.0, 500.0);

        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

        TransferRequest transferRequest = UserSteps.makeTransferRequest(senderAccount,receiverAccount, 1000.0, 5000.0);

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

        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(0.0);
    }

    @Tag("POSITIVE")
    @MethodSource("validTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanTransferWithValidAmountsTest(double depositAmount, double transferAmount) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

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


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() * 2 - transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotTransferWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser();
        ProfileModel userToDelete = UserSteps.getProfile(createUserRequest);
        addUserForCleanup(userToDelete);

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

        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(0.0);

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
                Arguments.of(0.0, ErrorText.smallTransferAmountError.getTitle()),
                Arguments.of(-100.0, ErrorText.smallTransferAmountError.getTitle()),
                Arguments.of(10000.01, ErrorText.hugeTransferAmountError.getTitle()),
                Arguments.of(-0.1, ErrorText.smallTransferAmountError.getTitle())
        );
    }
}
