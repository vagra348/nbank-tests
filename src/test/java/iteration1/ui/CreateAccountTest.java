package iteration1.ui;

import base.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.AccountModel;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseTest {
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
    public void userCanCreateAccountTest() {
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byXpath("//button[contains(text(),'Create New Account')]")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ New Account Created! Account Number:");
        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);
        matcher.find();
        String createdAccNum = matcher.group(1);

        AccountModel[] existingAccs = given()
                .spec(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(AccountModel[].class);

        assertThat(existingAccs).hasSize(1);

        AccountModel createdAcc = existingAccs[0];

        assertThat(createdAcc).isNotNull();
        assertThat(createdAcc.getAccountNumber()).isEqualTo(createdAccNum);
        assertThat(createdAcc.getBalance()).isZero();

    }
}
