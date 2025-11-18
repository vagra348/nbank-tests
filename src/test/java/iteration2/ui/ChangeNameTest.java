package iteration2.ui;

import base.BaseTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.ProfileModel;
import org.apache.http.HttpStatus;
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

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseTest {
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
    public void userCanChangeNameWithValidDataTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

//        3. Основная проверка - изменение имени
        String newName = RandomData.qenerateName();
        $(Selectors.byXpath("//*[@class='user-name']")).click();
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.byXpath("//button[contains(text(),'Save Changes')]")).click();
        Alert alert = switchTo().alert();

//        4. Проверки на фронте
        assertThat(alert.getText()).isEqualTo("✅ Name updated successfully!");
        alert.accept();
        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible);
        $(Selectors.byClassName("welcome-text")).shouldHave(Condition.partialText(newName));

//        5. Проверки на апи
        ProfileModel changedUser = given()
                .spec(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(ProfileModel.class);
        assertThat(changedUser.getName()).isEqualTo(newName);
    }

    @Tag("NEGATIVE")
    @Test
    public void userCanNotChangeNameWithInvalidDataTest() {

//        1. Создание пользователя
        CreateUserRequest createUserRequest = AdminSteps.createNewUser(this);
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(createUserRequest.getUsername()).password(createUserRequest.getPassword()).build())
                .extract().header("Authorization");

//        2. Авторизация
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

//        3. Основная проверка - изменение имени
        $(Selectors.byXpath("//*[@class='user-name']")).click();
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys("oneWord");
        $(Selectors.byXpath("//button[contains(text(),'Save Changes')]")).click();
        Alert alert = switchTo().alert();

//        4. Проверки на фронте
        assertThat(alert.getText()).isEqualTo("❌ Please enter a valid name.");
        alert.accept();
        Selenide.open("/dashboard");
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible);
        $(Selectors.byClassName("welcome-text")).shouldHave(Condition.partialText("noname"));

//        5. Проверки на апи
        ProfileModel changedUser = given()
                .spec(RequestSpecs.authUserSpec(createUserRequest.getUsername(), createUserRequest.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(ProfileModel.class);
        assertThat(changedUser.getName()).isEqualTo(null);
    }
}
