package iteration2;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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

        Integer senderAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(2000.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(999.0)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        new GetTransactionsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccountId)
                // и здесь тоже сложные json-ы для сравнения
                .body(Matchers.containsString("TRANSFER_OUT"));

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo((float) 999.0);
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

    Integer senderAccountId = new CreateAccountRequester(
            RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
            ResponseSpecs.entityWasCreated())
            .post().extract()
            .path("id");

    Integer receiverAccountId = new CreateAccountRequester(
            RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
            ResponseSpecs.entityWasCreated())
            .post().extract()
            .path("id");

    MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
            .id(senderAccountId)
            .balance(2000.0)
            .build();

    new MakeDepositRequester(
            RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
            ResponseSpecs.requestReturnsOK())
            .post(makeDepositRequest);

    TransferRequest transferRequest = TransferRequest.builder()
            .senderAccountId(senderAccountId)
            .receiverAccountId(receiverAccountId)
            .amount(999.0)
            .build();

    new TransferRequester(
            RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
            ResponseSpecs.requestReturnsOK())
            .post(transferRequest);

    new GetTransactionsRequester(
            RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
            ResponseSpecs.requestReturnsOK())
            .get(senderAccountId)
            .assertThat()
            .body(Matchers.containsString("DEPOSIT"))
            .body(Matchers.containsString("TRANSFER_OUT"));
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

        Integer senderAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(user2Request.getUsername(), user2Request.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(2000.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(999.0)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        new GetTransactionsRequester(
                RequestSpecs.authUserSpec(user1Request.getUsername(), user1Request.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccountId)
                // и здесь тоже сложные json-ы для сравнения
                .body(Matchers.containsString("TRANSFER_OUT"));

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo((float) 999.0);

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

        Integer senderAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(500.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(1000.0)
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(ErrorText.invalidTransferError.getTitle()))
                .post(transferRequest);
    }

    @Tag("POSITIVE")
    @MethodSource("validTransferAmounts")
    @ParameterizedTest
    public void authorizedUserCanTransferWithValidAmountsTest(double amount) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.qenerateUsername())
                .password(RandomData.qeneratePassword())
                .role(String.valueOf(UserRole.USER))
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        Integer senderAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(amount)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        TransferResponse transferResponse = new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        new GetTransactionsRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(senderAccountId)
                // и здесь тоже сложные json-ы для сравнения
                .body(Matchers.containsString("TRANSFER_OUT"));

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo(amount);
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

        Integer senderAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        Integer receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post().extract()
                .path("id");

        MakeDepositRequest makeDepositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(500.0)
                .build();

        new MakeDepositRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(makeDepositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.badRequest(errorValue))
                .post(transferRequest);
    }

    public static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(1.0),
                Arguments.of(1.1),
                Arguments.of(4999.9),
                Arguments.of(5000.0)
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
