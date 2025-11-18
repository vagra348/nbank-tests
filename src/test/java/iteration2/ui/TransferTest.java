package iteration2.ui;

import base.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.AccountModel;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.MakeDepositRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class TransferTest extends BaseTest {
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
    public void userCanTransferBetweenOwnAccountsTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация, создание аккаунта и депозит
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");
        AccountModel senderAccount = UserSteps.createAccount(createUserRequest);
        AccountModel receiverAccount = UserSteps.createAccount(createUserRequest);
        double sum = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, sum);
        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

//        3. Перевод



    }

    @Tag("POSITIVE")
    @Test
    public void userCanNotTransferWithoutEnteredSumTest() {
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotTransferMoreThanBalanceTest() {
    }

}
