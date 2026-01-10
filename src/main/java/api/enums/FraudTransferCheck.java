package api.enums;

import lombok.Getter;

@Getter
public enum FraudTransferCheck {
    FRAUD_APPROVED("APPROVED"),
    FRAUD_APPROVED_MSG("Transfer approved and processed immediately");

    private final String title;

    FraudTransferCheck(String title) {
        this.title = title;
    }
}