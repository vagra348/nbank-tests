package api.enums;

import lombok.Getter;

@Getter
public enum ErrorText {
    invalidAuthData("Invalid username or password"),
    usernameExtraSymbolError("Username must contain only letters, digits, dashes, underscores, and dots"),
    blankUsernameError("Username cannot be blank"),
    blankPasswordError("Password cannot be blank"),
    roleValidationError("Role must be either 'ADMIN' or 'USER'"),
    passwordValidationError("Password must contain at least one digit, one lower case, one upper case, one special "
            + "character, no spaces, and be at least 8 characters long"),
    usernameLengthError("Username must be between 3 and 15 characters"),
    invalidName("Name must contain two words with letters only"),
    invalidDepositAmount("Invalid account or amount"),
    hugeDepositAmountError("Deposit amount exceeds the 5000 limit"),
    unauthAccessError("Unauthorized access to account"),
    invalidTransferError("Invalid transfer: insufficient funds or invalid accounts"),
    hugeTransferAmountError("Transfer amount cannot exceed 10000");

    private final String title;

    ErrorText(String title) {
        this.title = title;
    }
}
