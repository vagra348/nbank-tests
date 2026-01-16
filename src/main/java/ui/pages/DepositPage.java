package ui.pages;

import api.models.AccountModel;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositPage extends BasePage<DepositPage> {
    private SelenideElement selectAccSelect = $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select"));
    private SelenideElement depositAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositBtn = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    public DepositPage makeDeposit(AccountModel newAcc, Double sum) {
        selectAccSelect.click();
        selectAccSelect.selectOptionByValue(String.valueOf(newAcc.getId()));
        if (sum != null) {
            depositAmountInput.sendKeys(String.valueOf(sum));
        }
        depositBtn.click();
        return this;
    }

    @Override
    public String url() {
        return "/deposit";
    }
}
