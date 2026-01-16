package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passInput = $(Selectors.byAttribute("placeholder", "Password"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText())
                .as("Alert text mismatch. Expected to contain: '%s', but was: '%s'",
                        bankAlert, alert.getText())
                .contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public static void authWithToken(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authWithToken(CreateUserRequest user) {
        Selenide.open("/");
        authWithToken(user.getUsername(), user.getPassword());
    }

    protected <T extends BaseElement> T generatePageElement(SelenideElement element,
                                                            Function<SelenideElement,
                                                                    T> constructor) {
        return constructor.apply(element);
    }

    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection,
                                                                   Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}
