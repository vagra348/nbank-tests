package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class WelcomeTitle extends BaseElement {
    private String welcomeText;
    private String userName;
    private String znak;

    public WelcomeTitle(SelenideElement element) {
        super(element);
        welcomeText = "Welcome,";
        userName = element.$("span").getText();
        znak = "!";
    }
}
