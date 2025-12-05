package ui.pages;

import api.models.AccountModel;
import api.models.CreateUserRequest;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement chooseAccSelect = $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*"));
    private SelenideElement recipientNameInput = $(Selectors.byPlaceholder("Enter recipient name"));
    private SelenideElement recipientAccNumInput = $(Selectors.byPlaceholder("Enter recipient account number"));
    private SelenideElement transferAmountInput = $(Selectors.byPlaceholder("Enter amount"));
    private SelenideElement confirmBtn = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferBtn = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private SelenideElement transferAgainBtn = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
    private ElementsCollection allTransactionsList = $(Selectors.byXpath("//*[text()='Matching Transactions']/parent::*/ul")).findAll("li");
    public ElementsCollection getAllTransactionsList() {
        return $(Selectors.byText("Matching Transactions"))
                .parent()
                .find("ul")
                .findAll("li");
    }

    public TransferPage makeTransfer(CreateUserRequest user, AccountModel senAcc, AccountModel recAcc, Double sum) {
        chooseAccSelect.click();
        chooseAccSelect.selectOptionByValue(String.valueOf(senAcc.getId()));
        recipientNameInput.sendKeys(user.getUsername());
        recipientAccNumInput.sendKeys(recAcc.getAccountNumber());
        if (sum != null) {
            transferAmountInput.sendKeys(String.valueOf(sum));
        }
        confirmBtn.click();
        sendTransferBtn.click();
        return this;
    }

    @Override
    public String url() {
        return "/transfer";
    }
}
