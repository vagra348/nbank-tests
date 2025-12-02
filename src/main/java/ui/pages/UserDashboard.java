package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createAccBtn = $(Selectors.byXpath("//button[contains(text(),'Create New Account')]"));
    private SelenideElement depositMoneyBtn = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferMoneyBtn = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement userNameProfile = $(Selectors.byXpath("//*[@class='user-name']"));

    public UserDashboard createNewAccount() {
        createAccBtn.click();
        return this;
    }

    @Override
    public String url() {
        return "/dashboard";
    }
}
