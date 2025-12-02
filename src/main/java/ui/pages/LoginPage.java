package ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

public class LoginPage extends BasePage<LoginPage> {

    private SelenideElement admitBtn = $("button");

    public LoginPage login(String username, String password) {
        usernameInput.sendKeys(username);
        passInput.sendKeys(password);
        admitBtn.click();
        return this;
    }
    public String url() {
        return "/login";
    }

    public LoginPage closeAlertIfPresent(int timeoutSeconds, String action) {
        try {
            new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(timeoutSeconds));
            Alert alert = switchTo().alert();
            switch (action) {
                case "accept":
                    alert.accept();
                case "dismiss":
                    alert.dismiss();
            }
            return this;
        } catch (Exception e) {
            return this;
        }
    }

}
