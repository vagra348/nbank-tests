package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import ui.elements.WelcomeTitle;

import java.util.Objects;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    public static final String WELCOME_NONAME_TEXT = "Welcome, noname!";
    public static final String NO_NAME_TEXT = "noname";
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createAccBtn = $(Selectors.byXpath("//button[contains(text(),'Create New Account')]"));
    private SelenideElement depositMoneyBtn = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferMoneyBtn = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement userNameProfile = $(Selectors.byXpath("//*[@class='user-name']"));

    public UserDashboard createNewAccount(String alertName) {
        RetryUtils.retryVoidWithCheck(
                () -> {
                    createAccBtn.shouldBe(Condition.visible);
                    createAccBtn.click();
                },
                () -> switchTo().alert().getText().contains(alertName),
                3,
                1000
        );

        return this;
    }

    public WelcomeTitle getWelcomeTitle() {
        return generatePageElement(welcomeText, WelcomeTitle::new);
    }

    public String getUserNameInWelcomeText() {
        return RetryUtils.retry(
                () -> getWelcomeTitle().getUserName(),
                result -> !Objects.equals(getWelcomeTitle().getUserName(), "noname"),
                7,
                2000
        );
    }

    @Override
    public String url() {
        return "/dashboard";
    }
}
