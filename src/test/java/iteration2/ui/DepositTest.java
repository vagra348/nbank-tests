package iteration2.ui;

import base.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
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
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
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
    public void userCanMakeDepositWithValidAmountTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация и создание аккаунта
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");
        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

//        3. Основная проверка - успешное пополнение с валидной суммой
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select/option[@value="+ newAcc.getId() +"]")).click();
        double sum = RandomData.generateSum(1.0, 5000.0);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(sum));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

//        4. Проверка на фронте
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited");
        alert.accept();

//        5. Проверка на апи
        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));
        AccountModel firstAcc = accounts.getFirst();
        assertThat(firstAcc.getBalance()).isEqualTo(sum);
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotMakeDepositWithInvalidAmountTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация и создание аккаунта
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");
        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

//        3. Основная проверка - безуспешное пополнение с невалидной суммой
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select/option[@value="+ newAcc.getId() +"]")).click();
        double sum = RandomData.generateSum(5000.0, 10000.0);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(sum));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

//        4. Проверка на фронте
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();

//        5. Проверка на апи
        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));
        AccountModel firstAcc = accounts.getFirst();
        assertThat(firstAcc.getBalance()).isZero();
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotMakeDepositWithoutEnteredSumTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация и создание аккаунта
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");
        AccountModel newAcc = UserSteps.createAccount(createUserRequest);

//        3. Основная проверка - попытка пополнения без указания суммы
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select")).click();
        $(Selectors.byXpath("//*[text()='Select Account:']/parent::*/select/option[@value=" + newAcc.getId() + "]")).click();
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

//        4. Проверка на фронте
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please enter a valid amount.");
        alert.accept();
    }
}
