package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CH("Username must be between 3 and 15 characters"),
    ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    INVALID_NAME("❌ Please enter a valid name."),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited"),
    DEPOSIT_LESS_OR_EQUAL_TO_5000("❌ Please deposit less or equal to 5000$."),
    ENTER_VALID_AMOUNT("❌ Please enter a valid amount."),
    SUCCESSFULLY_TRANSFERED("✅ Successfully transferred"),
    FILL_ALL_FIELDS("❌ Please fill all fields and confirm."),
    INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
