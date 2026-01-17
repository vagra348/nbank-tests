package common.extensions;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

public class AllureFailureAttachments implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent() &&
                WebDriverRunner.hasWebDriverStarted()) {

            takeScreenshot();
            takePageSource();
            takeBrowserLogs();
        }
    }

    private void takeScreenshot() {
        try {
            Object webDriver = WebDriverRunner.getWebDriver();

            if (webDriver instanceof TakesScreenshot) {
                TakesScreenshot screenshotDriver = (TakesScreenshot) webDriver;
                byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);

                if (screenshot != null && screenshot.length > 0) {
                    Allure.addAttachment("SCREENSHOT ON FAILURE",
                            "image/png",
                            new ByteArrayInputStream(screenshot),
                            ".png");
                } else {
                    System.err.println("Screenshot is null or empty");
                }
            } else {
                System.err.println("Driver doesn't implement TakesScreenshot");
            }
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void takePageSource() {
        try {
            String pageSource = WebDriverRunner.getWebDriver().getPageSource();
            Allure.addAttachment("PAGE SOURCE", "text/html", pageSource);
        } catch (Exception e) {
            System.err.println("Failed to get page source: " + e.getMessage());
        }
    }

    private void takeBrowserLogs() {
        try {
            StringBuilder logs = new StringBuilder();
            logs.append("URL: ").append(WebDriverRunner.url()).append("\n");
            logs.append("Browser: ").append(WebDriverRunner.getWebDriver()
                    .getClass().getSimpleName()).append("\n");

            Allure.addAttachment("BROWSER INFO", "text/plain", logs.toString());
        } catch (Exception e) {
        }
    }
}