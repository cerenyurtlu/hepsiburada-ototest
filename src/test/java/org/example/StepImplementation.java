package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.gauge.Step;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.*;

public class StepImplementation {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Actions actions;

    private static final String ELEMENTS_PATH = "src/test/resources/element-infos/elements.json";
    private static final String VALUES_PATH   = "src/test/resources/values-infos/values.json";

    private static LocatorHelper locatorHelper;
    private static Map<String, String> values;

    private void ensureInit() {
        if (driver != null) return;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-features=FedCm,IdentityCredential,InterestCohort");
        options.addArguments("--lang=tr-TR");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);

        locatorHelper = new LocatorHelper(ELEMENTS_PATH);
        values        = loadValues();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadValues() {
        try {
            return new ObjectMapper().readValue(new File(VALUES_PATH), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("values.json okunamadi: " + VALUES_PATH, e);
        }
    }

    private String getValue(String key) {
        String v = values.get(key);
        if (v == null) throw new RuntimeException("values.json'da key bulunamadi: " + key);
        return v;
    }

    private void waitPageReady() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"))
        );
    }

    // ----------------------------------------------------------------
    // # Anasayfa acilir
    // * "baseURL" adresine gidilir
    // ----------------------------------------------------------------

    @Step("<urlKey> adresine gidilir")
    public void goToUrl(String urlKey) {
        ensureInit();
        // Gauge "baseURL" string'ini gonderiyor, values.json'dan karsiligini aliyoruz
        String url = values.containsKey(urlKey) ? getValue(urlKey) : urlKey;
        System.out.println(">>> Gidilecek URL: " + url);
        driver.get(url);
        waitPageReady();
    }

    // ----------------------------------------------------------------
    // # Cerez bildirimi varsa kabul edilir
    // * <home.cookieAccept> elementi varsa tiklanir
    // ----------------------------------------------------------------

    @Step("<elementKey> elementi varsa tiklanir")
    public void clickIfExists(String elementKey) {
        ensureInit();
        System.out.println(">>> Cerez elementi araniyor: " + elementKey);
        By by = locatorHelper.getBy(elementKey);

        try {
            Thread.sleep(2000);
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(by));
            el.click();
            System.out.println(">>> Cerez kabul edildi!");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(">>> Cerez butonu bulunamadi veya tiklanamadi, devam ediliyor.");
        }
    }

    // ----------------------------------------------------------------
    // # Login butonuna tiklanir
    // * "home.loginMenu" elementi tiklanir
    // ----------------------------------------------------------------

    @Step("<elementKey> elementi tiklanir")
    public void clickElement(String elementKey) {
        ensureInit();
        System.out.println(">>> Tiklanacak element: " + elementKey);
        By by = locatorHelper.getBy(elementKey);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
                WebElement el = driver.findElement(by);
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                Thread.sleep(300);
                el.click();
                System.out.println(">>> Tiklama basarili!");
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println(">>> Deneme " + (i + 1) + " stale, tekrar deneniyor...");
            } catch (Exception e) {
                System.out.println(">>> Normal click basarisiz, JS ile deneniyor...");
                try {
                    WebElement el = driver.findElement(by);
                    js.executeScript("arguments[0].click();", el);
                    System.out.println(">>> JS tiklama basarili!");
                    return;
                } catch (Exception ignored) {}
            }
        }
        throw new RuntimeException("Click 3 denemede basarisiz: " + elementKey);
    }

    // ----------------------------------------------------------------
    // # Gecerli kullanici email ve password girilir
    // * "login.emailInput" alanina "LoginEmail" degerini yazar
    // * "login.passwordInput" alanina "LoginSifre" degerini yazar
    // * "login.submitButton" elementi tiklanir
    // * "home.accountArea" elementi gorunur olana kadar beklenir
    // ---------------------------------------------------------------

    @Step("<elementKey> alanina <valueKey> degerini yazar")
    public void typeValue(String elementKey, String valueKey) {
        ensureInit();
        System.out.println(">>> Yazilacak alan: " + elementKey + ", deger key: " + valueKey);
        By by = locatorHelper.getBy(elementKey);
        String text = values.containsKey(valueKey) ? getValue(valueKey) : valueKey;
        JavascriptExecutor js = (JavascriptExecutor) driver;

        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
                WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                Thread.sleep(300);
                js.executeScript("arguments[0].click();", el);
                Thread.sleep(300);
                el.sendKeys(text);
                System.out.println(">>> Yazma basarili!");
                return;
            } catch (StaleElementReferenceException | InvalidElementStateException e) {
                System.out.println(">>> Deneme " + (i + 1) + " basarisiz: " + e.getMessage());
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Yazma 3 denemede basarisiz: " + elementKey);
    }

    @Step("<elementKey> elementi gorunur olana kadar beklenir")
    public void waitForElementVisible(String elementKey) {
        ensureInit();
        System.out.println(">>> Element bekleniyor: " + elementKey);
        By by = locatorHelper.getBy(elementKey);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        System.out.println(">>> Element gorunur oldu: " + elementKey);
    }

    // ----------------------------------------------------------------
    // # Arama cubuguna yazilir ve arama yapilir
    // * "search.input" alanina "SearchText" degerini yazar
    // * Enter tusuna basilir
    // ---------------------------------------------------------------

    @Step("Enter tusuna basilir")
    public void pressEnter() {
        ensureInit();
        System.out.println(">>> Enter tusuna basiliyor...");
        try {
            Thread.sleep(500);
            WebElement activeEl = driver.switchTo().activeElement();
            activeEl.sendKeys(Keys.ENTER);
            System.out.println(">>> Enter basildi!");
            waitPageReady();
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    // ----------------------------------------------------------------
    // # Ikinci satirdaki ilk urun sepete eklenir
    // * "2" satir "1" sutundaki urunu sepete ekle
    // ---------------------------------------------------------------

    private static String savedProductName;

    @Step("<row> satir <col> sutundaki urunu sepete ekle")
    public void addToCartByPosition(String row, String col) {
        ensureInit();
        System.out.println(">>> " + row + ". satir " + col + ". sutundaki urun sepete ekleniyor...");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            Thread.sleep(2000);

            int index = (Integer.parseInt(row) - 1) * 4 + (Integer.parseInt(col) - 1);
            String liXpath = "//li[@id='i" + index + "']";
            System.out.println(">>> Hedef li: i" + index);

            WebElement li = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(liXpath)));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", li);
            Thread.sleep(1000);

            // Urun adini kaydet
            try {
                WebElement titleEl = li.findElement(By.cssSelector("a.title-module_titleText__8FlNQ"));
                savedProductName = titleEl.getAttribute("title").trim();
            } catch (Exception e) {
                savedProductName = li.getText().split("\n")[0].trim();
            }
            System.out.println(">>> Kaydedilen urun adi: " + savedProductName);

            // Sepete ekle butonuna tikla
            WebElement addBtn = li.findElement(By.cssSelector("div[role='button']"));
            js.executeScript("arguments[0].click();", addBtn);
            System.out.println(">>> Sepete ekle ikonuna tiklandi!");
            Thread.sleep(2000);

            // Popup cikarsa "Sepete ekle" butonuna tikla
            try {
                WebElement popupBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[data-test-id='variant-add-to-cart-button']")));
                popupBtn.click();
                System.out.println(">>> Popup Sepete ekle tiklandi!");
            } catch (Exception e) {
                System.out.println(">>> Popup cikmadi, devam ediliyor.");
            }

            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
    }

    // ----------------------------------------------------------------
    // # Sepetteki urun dogrulanir
    // * Sepetteki urun adi kontrol edilir
    // ---------------------------------------------------------------

    @Step("Sepetteki urun adi kontrol edilir")
    public void verifyCartProduct() {
        ensureInit();
        System.out.println(">>> Sepet kontrol ediliyor...");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            Thread.sleep(2000);

            // Sepetim'e tikla
            WebElement cartIcon = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("shoppingCart")));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", cartIcon);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", cartIcon);
            waitPageReady();
            Thread.sleep(3000);

            // Sepetteki urun adini al - href icinde magaza= olan linkler urun linkleri
            List<WebElement> productLinks = driver.findElements(By.cssSelector("a[href*='magaza=']"));
            String cartProductName = "";
            for (WebElement link : productLinks) {
                String text = link.getText().trim();
                if (!text.isEmpty()) {
                    cartProductName = text;
                    break;
                }
            }

            System.out.println(">>> Sepetteki urun: " + cartProductName);
            System.out.println(">>> Beklenen urun: " + savedProductName);

            String expected = savedProductName.toLowerCase().substring(0, Math.min(20, savedProductName.length()));
            String actual = cartProductName.toLowerCase();

            if (savedProductName != null && actual.contains(expected)) {
                System.out.println(">>> DOGRULAMA BASARILI! Urun sepette!");
            } else {
                throw new RuntimeException("DOGRULAMA BASARISIZ! Beklenen: " + savedProductName + " Bulunan: " + cartProductName);
            }
        } catch (InterruptedException ignored) {}
    }

    // ----------------------------------------------------------------
    // # Urun sepetten silinir ve sepet bos dogrulanir
    // * Sepetteki urun silinir
    // * Sepetin bos oldugu dogrulanir
    // ---------------------------------------------------------------

    @Step("Sepetteki urun silinir")
    public void removeFromCart() {
        ensureInit();
        System.out.println(">>> Sepetteki urun siliniyor...");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            Thread.sleep(2000);
            WebElement removeBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[aria-label='Sepetten Çıkar']")));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", removeBtn);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", removeBtn);
            System.out.println(">>> Sil butonuna tiklandi!");
            Thread.sleep(2000);

            // Popup cikarsa "Sil" butonuna tikla
            try {
                WebElement popupSil = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button.favoritesButton_3avg1")));
                popupSil.click();
                System.out.println(">>> Popup Sil butonuna tiklandi!");
            } catch (Exception e) {
                System.out.println(">>> Popup cikmadi, devam ediliyor.");
            }

            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
    }

    @Step("Sepetin bos oldugu dogrulanir")
    public void verifyCartEmpty() {
        ensureInit();
        System.out.println(">>> Sepetin bos oldugu kontrol ediliyor...");

        try {
            Thread.sleep(2000);
            WebElement emptyText = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h1[contains(text(),'boş') or contains(text(),'Boş') or contains(text(),'bos')]")));
            String text = emptyText.getText().trim();
            System.out.println(">>> Sepet mesaji: " + text);
            System.out.println(">>> DOGRULAMA BASARILI! Sepet bos!");
        } catch (Exception e) {
            throw new RuntimeException("DOGRULAMA BASARISIZ! Sepet bos mesaji bulunamadi!");
        }
    }
}