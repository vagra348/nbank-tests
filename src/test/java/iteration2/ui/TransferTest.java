package iteration2.ui;

import base.BaseTest;
import com.codeborne.selenide.*;
import generators.RandomData;
import models.AccountModel;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.MakeDepositRequest;
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
        double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

//        3. Перевод
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*/option[@value="+ senderAccount.getId() +"]")).click();
        $(Selectors.byPlaceholder("Enter recipient name")).sendKeys(createUserRequest.getUsername());
        $(Selectors.byPlaceholder("Enter recipient account number")).sendKeys(receiverAccount.getAccountNumber());
        double transferAmount = RandomData.generateSum(1.0, 1999.0);
        $(Selectors.byPlaceholder("Enter amount")).sendKeys(String.valueOf(transferAmount));
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

//        4. Ui проверка
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred");
        alert.accept();
        Selenide.open("/transfer");
        $(Selectors.byText("\uD83D\uDD01 Transfer Again")).click();
        ElementsCollection allTransactions = $(Selectors.byXpath("//*[text()='Matching Transactions']/parent::*/ul")).findAll("li");
        allTransactions.findBy(Condition.partialText(String.format("%.2f", transferAmount))).shouldBe(Condition.visible);

//        5. Api проверка
        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));
        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance() - transferAmount);
        assertThat(changedReceiverAcc.getBalance()).isEqualTo(transferAmount);
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotTransferWithoutEnteredSumTest() {

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
        double depositAmount = RandomData.generateSum(2000.0, 5000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

//        3. Перевод
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*/option[@value="+ senderAccount.getId() +"]")).click();
        $(Selectors.byPlaceholder("Enter recipient name")).sendKeys(createUserRequest.getUsername());
        $(Selectors.byPlaceholder("Enter recipient account number")).sendKeys(receiverAccount.getAccountNumber());
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

//        4. Ui проверка
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

//        5. Api проверка
        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));
        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        assertThat(changedReceiverAcc.getBalance()).isZero();
    }


    @Tag("NEGATIVE")
    @Test
    public void userCanNotTransferMoreThanBalanceTest() {

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
        double depositAmount = RandomData.generateSum(1000.0, 2000.0);
        MakeDepositRequest makeDepositRequest = UserSteps.makeDepositRequest(senderAccount, depositAmount);
        UserSteps.makeDeposit(createUserRequest, makeDepositRequest);

//        3. Перевод
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*")).click();
        $(Selectors.byXpath("//*[text()='-- Choose an account --']/parent::*/option[@value="+ senderAccount.getId() +"]")).click();
        $(Selectors.byPlaceholder("Enter recipient name")).sendKeys(createUserRequest.getUsername());
        $(Selectors.byPlaceholder("Enter recipient account number")).sendKeys(receiverAccount.getAccountNumber());
        double transferAmount = RandomData.generateSum(2001.0, 5000.0);
        $(Selectors.byPlaceholder("Enter amount")).sendKeys(String.valueOf(transferAmount));
        $(Selectors.byId("confirmCheck")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

//        4. Ui проверка
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();

//        5. Api проверка
        List<AccountModel> accounts = List.of(UserSteps.getAccounts(createUserRequest)
                .extract().as(AccountModel[].class));
        AccountModel changedSenderAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(senderAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(senderAccount.getId())));
        AccountModel changedReceiverAcc = accounts.stream()
                .filter(acc -> acc.getId().equals(receiverAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transaction with id %d not found".formatted(receiverAccount.getId())));
        assertThat(changedSenderAcc.getBalance()).isEqualTo(makeDepositRequest.getBalance());
        assertThat(changedReceiverAcc.getBalance()).isZero();
    }

}
