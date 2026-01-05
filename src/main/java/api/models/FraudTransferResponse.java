package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudTransferResponse extends BaseModel {
    private String status;
    private String message;
    private Integer transactionId;
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private double amount;
    private double fraudRiskScore;
    private String fraudReason;
    private boolean requiresVerification;
    private boolean requiresManualReview;
}