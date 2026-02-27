package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverFactory {

    private static WebDriver driver;

    private DriverFactory() {}

    public static WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
        }
        return driver;
    }

    private static WebDriver createDriver() {
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--disable-notifications");
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("--disable-features=FedCm,IdentityCredential,InterestCohort");
        opts.addArguments("--lang=tr-TR");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        opts.setExperimentalOption("prefs", prefs);
        opts.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        opts.setExperimentalOption("useAutomationExtension", false);

        WebDriverManager.chromedriver().setup();
        WebDriver d = new ChromeDriver(opts);
        d.manage().window().maximize();

        ((JavascriptExecutor) d).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        return d;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}