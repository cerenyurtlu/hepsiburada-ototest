package org.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Driver {

    private Driver() {}

    public static WebDriver get() {
        return DriverFactory.getDriver();
    }

    public static WebDriverWait getWait() {
        return new WebDriverWait(get(), Duration.ofSeconds(15));
    }

    public static Actions getActions() {
        return new Actions(get());
    }

    public static void quit() {
        DriverFactory.quitDriver();
    }
}