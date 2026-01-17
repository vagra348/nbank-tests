package api.requests.steps;

import api.generators.RandomData;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public List<AccountModel> getAllAccounts() {
        return StepLogger.log("getAllAccounts", () -> {
            return new ValidatedCrudRequester<AccountModel>(RequestSpecs.authUserSpec(this.username, this.password),
                    Endpoint.GET_USER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK()).getAll(AccountModel[].class);
        });
    }

    public static AccountModel createAccount(CreateUserRequest createUserRequest) {
        return StepLogger.log("createAccount", () -> {
            return new ValidatedCrudRequester<AccountModel>(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.ACCOUNTS,
                    ResponseSpecs.entityWasCreated())
                    .post(null);
        });
    }

    public static MakeDepositRequest makeDepositRequest(AccountModel account, double min, double max) {
        return StepLogger.log("makeDepositRequest", () -> {
            return MakeDepositRequest.builder()
                    .id(account.getId())
                    .balance(RandomData.generateSum(min, max))
                    .build();
        });
    }

    public static MakeDepositRequest makeDepositRequest(AccountModel account, double amount) {
        return StepLogger.log("makeDepositRequest", () -> {
            return MakeDepositRequest.builder()
                    .id(account.getId())
                    .balance(amount)
                    .build();
        });
    }

    public static TransferRequest makeTransferRequest(AccountModel senderAccount, AccountModel receiverAccount,
                                                      double min, double max) {
        return StepLogger.log("makeTransferRequest", () -> {
            return TransferRequest.builder()
                    .senderAccountId(senderAccount.getId())
                    .receiverAccountId(receiverAccount.getId())
                    .amount(RandomData.generateSum(min, max))
                    .build();
        });
    }

    public static TransferRequest makeTransferRequest(AccountModel senderAccount,
                                                      AccountModel receiverAccount, double amount) {
        return StepLogger.log("makeTransferRequest", () -> {
            return TransferRequest.builder()
                    .senderAccountId(senderAccount.getId())
                    .receiverAccountId(receiverAccount.getId())
                    .amount(amount)
                    .build();
        });
    }

    public static ValidatableResponse makeDeposit(CreateUserRequest createUserRequest,
                                                  MakeDepositRequest makeDepositRequest) {
        return StepLogger.log("makeDeposit", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_DEPOSIT,
                    ResponseSpecs.requestReturnsOK())
                    .post(makeDepositRequest);
        });
    }

    public static ValidatableResponse makeBadReqDeposit(CreateUserRequest createUserRequest,
                                                        MakeDepositRequest makeDepositRequest, String errorValue) {
        return StepLogger.log("makeBadReqDeposit", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_DEPOSIT,
                    ResponseSpecs.badRequest(errorValue))
                    .post(makeDepositRequest);
        });
    }

    public static ValidatableResponse makeForbiddenDeposit(CreateUserRequest createUserRequest,
                                                           MakeDepositRequest makeDepositRequest) {
        return StepLogger.log("makeForbiddenDeposit", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_DEPOSIT,
                    ResponseSpecs.requestForbidden())
                    .post(makeDepositRequest);
        });
    }

    public static TransferResponse makeTransfer(CreateUserRequest createUserRequest, TransferRequest transferRequest) {
        return StepLogger.log("makeTransfer", () -> {
            return new ValidatedCrudRequester<TransferResponse>(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_TRANSFER,
                    ResponseSpecs.requestReturnsOK())
                    .post(transferRequest);
        });
    }

    public static ValidatableResponse makeTransferBadReq(CreateUserRequest createUserRequest,
                                                         TransferRequest transferRequest, String errorValue) {
        return StepLogger.log("makeTransferBadReq", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_TRANSFER,
                    ResponseSpecs.badRequest(errorValue))
                    .post(transferRequest);
        });
    }

    public static ChangeNameResponse changeName(CreateUserRequest createUserRequest,
                                                ChangeNameRequest changeNameRequest) {
        return StepLogger.log("changeName", () -> {
            return new ValidatedCrudRequester<ChangeNameResponse>(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.CHANGE_NAME,
                    ResponseSpecs.requestReturnsOK())
                    .update(changeNameRequest);
        });
    }

    public static ValidatableResponse changeNameBadReq(CreateUserRequest createUserRequest,
                                                       ChangeNameRequest changeNameRequest, String errorValue) {
        return StepLogger.log("changeNameBadReq", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.CHANGE_NAME,
                    ResponseSpecs.badRequest(errorValue))
                    .update(changeNameRequest);
        });
    }

    public static ProfileModel getProfile(CreateUserRequest createUserRequest) {
        return StepLogger.log("getProfile", () -> {
            return new ValidatedCrudRequester<ProfileModel>(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.GET_PROFILE,
                    ResponseSpecs.requestReturnsOK())
                    .get();
        });
    }

    public static ValidatableResponse getAccounts(CreateUserRequest createUserRequest) {
        return StepLogger.log("Get accounts", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.GET_USER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK())
                    .get();
        });
    }

    public static ValidatableResponse getTransactions(CreateUserRequest createUserRequest, AccountModel account) {
        return StepLogger.log("Get transactions", () -> {
            return new CrudRequester(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.TRANSACTIONS,
                    ResponseSpecs.requestReturnsOK())
                    .get("param", account.getId());
        });
    }

    public static FraudTransferRequest makeFraudTransferRequest(AccountModel senderAccount,
                                                                AccountModel receiverAccount,
                                                                double min, double max,
                                                                String description) {
        return StepLogger.log("makeFraudTransferRequest", () -> {
            return FraudTransferRequest.builder()
                    .senderAccountId(senderAccount.getId())
                    .receiverAccountId(receiverAccount.getId())
                    .amount(RandomData.generateSum(min, max))
                    .description(description)
                    .build();
        });
    }

    public static FraudTransferResponse transferWithFraudCheck(CreateUserRequest createUserRequest,
                                                               FraudTransferRequest transferRequest) {
        return StepLogger.log("transferWithFraudCheck", () -> {
            return new ValidatedCrudRequester<FraudTransferResponse>(
                    RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    Endpoint.MAKE_TRANSFER_WITH_FRAUD_CHECK,
                    ResponseSpecs.requestReturnsOK()).post(transferRequest);
        });
    }

}
