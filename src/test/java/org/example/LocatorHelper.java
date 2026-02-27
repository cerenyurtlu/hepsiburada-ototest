package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocatorHelper {

    private final Map<String, By> locators = new HashMap<>();

    public LocatorHelper(String elementsJsonPath) {
        try {
            JsonNode arr = new ObjectMapper().readTree(new File(elementsJsonPath));
            for (JsonNode node : arr) {
                String key   = node.get("key").asText();
                String value = node.get("value").asText();
                String type  = node.get("type").asText().toLowerCase();

                By by = switch (type) {
                    case "id"    -> By.id(value);
                    case "css"   -> By.cssSelector(value);
                    case "xpath" -> By.xpath(value);
                    case "class" -> By.className(value);
                    case "name"  -> By.name(value);
                    default      -> throw new RuntimeException("Bilinmeyen locator tipi: " + type);
                };

                locators.put(key, by);
            }
        } catch (Exception e) {
            throw new RuntimeException("elements.json okunamadi: " + elementsJsonPath, e);
        }
    }

    public By getBy(String key) {
        By by = locators.get(key);
        if (by == null) throw new RuntimeException("elements.json'da key bulunamadi: " + key);
        return by;
    }
}