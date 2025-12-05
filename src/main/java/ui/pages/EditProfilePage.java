package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class EditProfilePage extends BasePage<EditProfilePage>{
    private SelenideElement editProfileBtn = $(Selectors.byText("✏\uFE0F Edit Profile"));
    private SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesBtn = $(Selectors.byXpath("//button[contains(text(),'Save Changes')]"));


    public EditProfilePage changeName(String name) {
        editProfileBtn.shouldBe(Condition.visible);
        nameInput.sendKeys(name);
        saveChangesBtn.click();
        return this;
    }

    @Override
    public String url() {
        return "/edit-profile";
    }
}
