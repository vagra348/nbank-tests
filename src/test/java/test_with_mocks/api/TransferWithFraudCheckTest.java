package test_with_mocks.api;

import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import base.BaseTest;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    @Tag("POSITIVE") @Tag("api") @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false,
            port = 8085
    )
    public void testTransferWithFraudCheck() {
        CreateUserRequest user1Request = AdminSteps.createNewUser(this);
        CreateUserRequest user2Request = AdminSteps.createNewUser(this);
        AccountModel senderAccount = UserSteps.createAccount(user1Request);
        AccountModel receiverAccount = UserSteps.createAccount(user2Request);

        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, 2000.0, 5000.0);
        UserSteps.makeDeposit(user1Request, makeDepositRequest);

        FraudTransferRequest transferRequest = UserSteps.makeFraudTransferRequest(senderAccount,receiverAccount, 1.0, 1999.0, "Test transfer with fraud check");
        FraudTransferResponse transferResponse = UserSteps.transferWithFraudCheck(user1Request, transferRequest);

        softly.assertThat(transferResponse).isNotNull();

        FraudTransferResponse expectedResponse = FraudTransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferRequest.getAmount())
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModel(expectedResponse, transferResponse).match();
    }

}