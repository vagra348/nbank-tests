package iteration1.ui;

import base.BaseTest;
import com.codeborne.selenide.*;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.ProfileModel;
import models.comparison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import specs.RequestSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest extends BaseTest {
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
    public void adminCanCreateUserTest(){
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);


        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byXpath("//button[text()='Add User']")).click();

        Alert alert = switchTo().alert();

        assertEquals(alert.getText(), "✅ User created successfully!");
        alert.accept();

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.partialText(newUser.getUsername())).shouldBe(Condition.visible);

        ProfileModel[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(ProfileModel[].class);

        ProfileModel createdUser = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModel(newUser, createdUser).match();
    }

    @Tag("NEGATIVE")
    @Test
    public void adminCanNotCreateUserWithInvalidDataTest(){
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byXpath("//button[text()='Add User']")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText().contains("Username must be between 3 and 15 characters"));
        alert.accept();

        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.partialText(newUser.getUsername())).shouldNotBe(Condition.exist);

        ProfileModel[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(ProfileModel[].class);

        long usersWithSameUsrnmAsNewUser = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUsrnmAsNewUser).isZero();
    }
}
