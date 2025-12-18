package common.extensions;

import com.codeborne.selenide.Configuration;
import common.annotations.Browsers;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class BrowserMatcExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Browsers annotation = context.getElement().map(el -> el.getAnnotation(Browsers.class))
                .orElse(null);
        if (annotation == null) {
            return ConditionEvaluationResult.enabled("No requirements to browser");
        }

        String currentBrowser = Configuration.browser;
        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(browser -> browser.equals(currentBrowser));
        if (matches) {
            return ConditionEvaluationResult.enabled("Current browser passes the condition: " + currentBrowser);
        } {
            return ConditionEvaluationResult.disabled("Test missed, current browser doesn't pass the condition: "
                    + currentBrowser + ", allowed browsers: " + Arrays.toString(annotation.value()));
        }
    }
}





















