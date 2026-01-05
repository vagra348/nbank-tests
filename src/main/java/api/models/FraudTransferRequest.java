package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudTransferRequest extends BaseModel {
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private double amount;
    private String description;
}