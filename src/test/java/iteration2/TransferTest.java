package iteration2;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class TransferTest extends BaseTest {

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferBetweenOwnAccountsTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(RandomData.generateSum(2000.0, 5000.0))
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(RandomData.generateSum(1.0, 1999.0))
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccount.getId());
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccount.getId());
        softly.assertThat(transferResponse.getAmount()).isEqualTo(transferRequest.getAmount());


        List<TransactionModel> transactionsSenderAcc = List.of(new GetTransactionsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccount.getId())
                .extract().as(TransactionModel[].class));

        TransactionModel targetOutTransaction = transactionsSenderAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + receiverAccount.getId() + " not found"));

        List<TransactionModel> transactionsReceiverAcc = List.of(new GetTransactionsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(receiverAccount.getId())
                .extract().as(TransactionModel[].class));

        TransactionModel targetInTransaction = transactionsReceiverAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + senderAccount.getId() + " not found"));

        softly.assertThat(targetInTransaction.getAmount()).isEqualTo(transferRequest.getAmount());
        softly.assertThat(targetOutTransaction.getAmount()).isEqualTo(transferRequest.getAmount());

        softly.assertThat(targetInTransaction.getType()).isEqualTo("TRANSFER_IN");
        softly.assertThat(targetOutTransaction.getType()).isEqualTo("TRANSFER_OUT");

        List<AccountModel> accounts = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + senderAccount.getId() + " not found"));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + receiverAccount.getId() + " not found"));


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance()-transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());

    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanSeeTransactionsTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract().as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(RandomData.generateSum(2000.0, 5000.0))
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(RandomData.generateSum(1.0, 1999.0))
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest);

        List<TransactionModel> transactions = List.of(new GetTransactionsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccount.getId())
                .extract().as(TransactionModel[].class));

        TransactionModel targetDepositTransaction = transactions.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + senderAccount.getId() + " not found"));

        TransactionModel targetOutTransaction = transactions.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + receiverAccount.getId() + " not found"));

        softly.assertThat(targetDepositTransaction.getAmount()).isEqualTo(makeDepositRequest.getBalance());
        softly.assertThat(targetOutTransaction.getAmount()).isEqualTo(transferRequest.getAmount());

        softly.assertThat(targetDepositTransaction.getType()).isEqualTo("DEPOSIT");
        softly.assertThat(targetOutTransaction.getType()).isEqualTo("TRANSFER_OUT");

    }

    @Tag("POSITIVE")
    @Test
    public void authorizedUserCanTransferToOtherUserAccountTest() {
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

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(RandomData.generateSum(2000.0, 5000.0))
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(RandomData.generateSum(1.0, 1999.0))
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccount.getId());
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccount.getId());
        softly.assertThat(transferResponse.getAmount()).isEqualTo(transferRequest.getAmount());

        List<TransactionModel> transactionsSenderAcc = List.of(new GetTransactionsRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccount.getId())
                .extract().as(TransactionModel[].class));

        TransactionModel targetOutTransaction = transactionsSenderAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + receiverAccount.getId() + " not found"));

        List<TransactionModel> transactionsReceiverAcc = List.of(new GetTransactionsRequester(
                RequestSpecs.authUserSpec(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(receiverAccount.getId())
                .extract().as(TransactionModel[].class));

        TransactionModel targetInTransaction = transactionsReceiverAcc.stream()
                .filter(transaction -> transaction.getRelatedAccountId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id " + senderAccount.getId() + " not found"));

        softly.assertThat(targetInTransaction.getAmount()).isEqualTo(transferRequest.getAmount());
        softly.assertThat(targetOutTransaction.getAmount()).isEqualTo(transferRequest.getAmount());

        softly.assertThat(targetInTransaction.getType()).isEqualTo("TRANSFER_IN");
        softly.assertThat(targetOutTransaction.getType()).isEqualTo("TRANSFER_OUT");

        List<AccountModel> accountsUser1 = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accountsUser1.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + senderAccount.getId() + " not found"));

        List<AccountModel> accountsUser2 = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedReceiverAcc = accountsUser2.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + receiverAccount.getId() + " not found"));


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() - transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());

    }

    @Tag("NEGATIVE")
    @Test
    public void authorizedUserCanNotTransferMoreThanBalanceTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(RandomData.generateSum(1.0, 500.0))
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(RandomData.generateSum(1000.0, 5000.0))
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(ErrorText.invalidTransferError.getTitle()))
                .post(transferRequest);

        List<AccountModel> accounts = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + senderAccount.getId() + " not found"));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + receiverAccount.getId() + " not found"));

        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(0.0);
    }

    @Tag("POSITIVE")
    @MethodSource("validTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanTransferWithValidAmountsTest(double depositAmount, double transferAmount) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(depositAmount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(transferAmount)
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        List<AccountModel> accounts = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + senderAccount.getId() + " not found"));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + receiverAccount.getId() + " not found"));


        softly.assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance()*2 - transferRequest.getAmount());
        softly.assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferRequest.getAmount());
    }

    @Tag("NEGATIVE")
    @MethodSource("invalidTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanNotTransferWithInvalidAmountsTest(double amount, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        AccountModel senderAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        AccountModel receiverAccount = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .as(AccountModel.class);

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccount.getId())
                .balance(RandomData.generateSum(0.01, 5000.0))
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(errorValue))
                .post(transferRequest);

        List<AccountModel> accounts = List.of(new GetUserAccountsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract().as(AccountModel[].class));

        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + senderAccount.getId() + " not found"));

        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + receiverAccount.getId() + " not found"));

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
