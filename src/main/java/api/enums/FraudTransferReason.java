package api.enums;

import lombok.Getter;

@Getter
public enum FraudTransferReason {
    FRAUD_LOW_RISK_REASON("Low risk transaction");

    private final String title;

    FraudTransferReason(String title) {
        this.title = title;
    }
}
