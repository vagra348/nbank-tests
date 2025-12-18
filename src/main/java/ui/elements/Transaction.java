package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static java.lang.Double.parseDouble;

@Getter
public class Transaction extends BaseElement {
    private String transactionType;
    private double amount;

    public Transaction(SelenideElement element) {
        super(element);
        String firstLine = element.getText().split("\n")[0];
        int dashIndex = firstLine.indexOf(" - $");
        transactionType = firstLine.substring(0, dashIndex).trim();
        amount = parseDouble(firstLine.substring(dashIndex + 4).trim());
    }
}
