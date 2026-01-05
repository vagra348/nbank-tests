package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import ui.elements.UserBadge;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserBtn = $(Selectors.byXpath("//button[text()='Add User']"));

    public List<UserBadge> getAllUsers() {
        ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
        return generatePageElements(elementsCollection, UserBadge::new);
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passInput.sendKeys(password);
        addUserBtn.click();
        return this;
    }

    public UserBadge findUserByUsername(String username) {
        return RetryUtils.retry(
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                3,
                1000
        );
    }

    @Override
    public String url() {
        return "/admin";
    }
}
