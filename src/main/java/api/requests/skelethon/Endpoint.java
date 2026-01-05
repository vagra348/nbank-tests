package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_CREATE_USER(
            "/admin/users",
            CreateUserRequest.class,
            ProfileModel.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    MAKE_DEPOSIT(
            "/accounts/deposit",
            MakeDepositRequest.class,
            BaseModel.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            AccountModel.class
    ),

    MAKE_TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),

    ADMIN_GET_USERS_LIST(
            "/admin/users",
            BaseModel.class,
            BaseModel.class
    ),

    GET_USER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            BaseModel.class
    ),

    CHANGE_NAME(
            "/customer/profile",
            ChangeNameRequest.class,
            ChangeNameResponse.class
    ),

    GET_PROFILE(
            "/customer/profile",
            BaseModel.class,
            ProfileModel.class
    ),

    TRANSACTIONS(
            "/accounts/{param}/transactions",
            BaseModel.class,
            TransactionModel.class
    ),

    ADMIN_DELETE_USER(
            "/admin/users/{param}",
            BaseModel.class,
            BaseModel.class
    ),

    MAKE_TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check",
            TransferRequest.class,
            TransferResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/accounts/fraud-check/{transactionId}",
            BaseModel.class,
            FraudCheckResponse.class)
    ;

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

    public String buildUrlWithPathParam(Integer param) {
        return this.url.replace("{param}", param.toString());
    }
}
