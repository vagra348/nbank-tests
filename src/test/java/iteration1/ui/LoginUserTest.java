package iteration1.ui;

import base.BaseTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginUserTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid(){
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://10.43.94.165:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
                );
    }

    @Tag("POSITIVE")
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);
    }

    @Tag("POSITIVE")
    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createNewUser(this);

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible);
        $(Selectors.byText("User Dashboard")).shouldBe(Condition.visible);
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotLoginWithIncorrectDataTest() {
        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys("nonExistUser");
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys("somePass1$");
        $("button").click();

        Alert alert = switchTo().alert();
        assertEquals(alert.getText(), "Invalid credentialsAxiosError: Request failed with status code 401");
    }
}
